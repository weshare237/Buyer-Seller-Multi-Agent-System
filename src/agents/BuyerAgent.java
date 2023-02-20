package agents;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;

public class BuyerAgent extends GuiAgent {

    private BuyerGui buyerGui;
    protected AID[] sellers;

    @Override
    protected void setup() {
        if(getArguments().length > 0) {
            buyerGui = (BuyerGui) getArguments()[0];
            buyerGui.setBuyerAgent(this);
        }

        ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
        addBehaviour(parallelBehaviour);

        parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, 50000) {
            @Override
            protected void onTick() {
                DFAgentDescription agentDescription = new DFAgentDescription();
                ServiceDescription serviceDescription = new ServiceDescription();
                serviceDescription.setType("Transaction");
                serviceDescription.setName("Book Selling");
                agentDescription.addServices(serviceDescription);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, agentDescription);

                    sellers = new AID[result.length];

                    for (int i = 0; i < result.length; i++) {
                        sellers[i] = result[i].getName();
                    }
                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        parallelBehaviour.addSubBehaviour(new CyclicBehaviour() {
            private int counter = 0;
            List<ACLMessage> replies = new ArrayList<>();

            @Override
            public void action() {
                MessageTemplate messageTemplate = MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                        MessageTemplate.or(
                                MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
                                MessageTemplate.or(
                                        MessageTemplate.MatchPerformative(ACLMessage.AGREE),
                                        MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
                                )
                        )
                );
                ACLMessage message = receive(messageTemplate);

                if(message != null) {

                    switch (message.getPerformative()) {
                        case ACLMessage.REQUEST:
                            String book = message.getContent();
                            ACLMessage aclMessage = new ACLMessage(ACLMessage.CFP);
                            aclMessage.setContent(book);
                            for (AID aid : sellers) {
                                aclMessage.addReceiver(aid);
                            }
                            send(aclMessage);
                            break;

                        case ACLMessage.PROPOSE:
                            ++counter;
                            replies.add(message);
                            if(counter == sellers.length) {
                                ACLMessage bestOffer = replies.get(0);
                                double minPrice = Double.parseDouble(bestOffer.getContent());
                                for (ACLMessage offer : replies) {
                                    double price = Double.parseDouble(offer.getContent());
                                    if(price < minPrice) {
                                        bestOffer = offer;
                                        minPrice = price;
                                    }
                                }

                                ACLMessage aclAcceptMessage = bestOffer.createReply();
                                aclAcceptMessage.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                send(aclAcceptMessage);
                            }
                            break;

                        case ACLMessage.AGREE:
                            ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
                            reply.addReceiver(new AID("Consumer", AID.ISLOCALNAME));
                            reply.setContent(message.getContent());
                            send(reply);
                            break;

                        case ACLMessage.REFUSE:
                            break;

                        default:
                            break;
                    }

                    String book = message.getContent();

                    buyerGui.logMessage(message);
                    ACLMessage reply = message.createReply();
                    reply.setContent("Ok for " + book + " book");
                    send(reply);

                    ACLMessage aclMessage = new ACLMessage(ACLMessage.CFP);
                    aclMessage.setContent(book);
                    aclMessage.addReceiver(new AID("Seller", AID.ISLOCALNAME));
                    send(aclMessage);
                }
                else {
                    block();
                }
            }
        });
    }

    @Override
    public void onGuiEvent(GuiEvent guiEvent) {

    }
}
