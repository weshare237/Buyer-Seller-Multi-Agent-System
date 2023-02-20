package agents;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

import java.util.Random;

public class SellerAgent extends GuiAgent {

    private SellerGui sellerGui;

    @Override
    protected void setup() {
        if(getArguments().length > 0) {
            sellerGui = (SellerGui) getArguments()[0];
            sellerGui.setSellerAgent(this);
        }

        ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
        addBehaviour(parallelBehaviour);

        parallelBehaviour.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                DFAgentDescription agentDescription = new DFAgentDescription();
                agentDescription.setName(getAID());
                ServiceDescription serviceDescription = new ServiceDescription();
                serviceDescription.setType("Transaction");
                serviceDescription.setName("Book Selling");
                agentDescription.addServices(serviceDescription);
                try {
                    DFService.register(myAgent, agentDescription);
                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        parallelBehaviour.addSubBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage message = receive();
                if(message != null) {
                    sellerGui.logMessage(message);

                    switch (message.getPerformative()) {
                        case ACLMessage.CFP:
                            ACLMessage reply = message.createReply();
                            reply.setPerformative(ACLMessage.PROPOSE);
                            reply.setContent(String.valueOf(500 + new Random().nextInt(1000)));
                            send(reply);
                            break;

                        case ACLMessage.ACCEPT_PROPOSAL:
                            ACLMessage replyMessage = message.createReply();
                            replyMessage.setPerformative(ACLMessage.AGREE);
                            send(replyMessage);
                            break;

                        default:
                            break;
                    }
                }
                else {
                    block();
                }
            }
        });
    }

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {

    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
    }
}
