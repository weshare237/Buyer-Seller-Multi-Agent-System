package agents;

import containers.ConsumerContainer;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

public class ConsumerAgent extends GuiAgent {

    private transient ConsumerContainer gui;

    @Override
    protected void setup() {
        if(getArguments().length == 1) {
            gui = (ConsumerContainer) getArguments()[0];
            gui.setConsumerAgent(this);
        }

        ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
        addBehaviour(parallelBehaviour);

        parallelBehaviour.addSubBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage message = receive();
                if(message != null) {
                    switch (message.getPerformative()) {
                        case ACLMessage.CONFIRM:
                            gui.logMessage(message);
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
    public void onGuiEvent(GuiEvent guiEvent) {
        if(guiEvent.getType() == 1) {
            String book = guiEvent.getParameter(0).toString();
            ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
            aclMessage.setContent(book);
            aclMessage.addReceiver(new AID("Buyer", AID.ISLOCALNAME));
            send(aclMessage);
        }
    }
}
