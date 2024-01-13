/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package akka.projetsd.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import java.util.List;
import java.util.Objects;
import java.util.Random;


/**
 * Acteur symbolisant un processus dans l'algorithme d'éléction Chang-Roberts
 * @author noaba
 */
public class ProcessActor extends AbstractActor{
    
    /**
     * Si le processus participe a une election ou pas (faux par défaut)
     */
    private boolean participant;
    /**
     * Si le processus est leader ou pas (faux par défaut)
     */
    private boolean leader;
    
    /**
     * Rang du processus dans l'anneau (-1 par défaut)
     */
    private int rank;
    
    /**
     * Reference du prochain processus dans l'anneau (null par défaut)
     */
    private ActorRef nextActor;
    
    /**
     * UID du processus généré avec la methode hash de java lors de l'initialisation de l'anneau
     */
    private int UID;
    /**
     * UID du processus leader (-1 par défaut)
     */
    private int leaderUID;
    
    /**
     * Constructeur d'un acteur
     */
    private ProcessActor(){
        this.participant = false;
        this.leader = false;
        this.rank = -1;
        this.nextActor = null;
        this.UID = -1;
        this.leaderUID=-1;
    }  
    

    // Méthode servant à déterminer le comportement de l'acteur lorsqu'il reçoit un message
    @Override
    public AbstractActor.Receive createReceive() {
            return receiveBuilder()
                            .match(StartElectionMessage.class, message -> receivedStartElectionMessage())
                            .match(ElectionMessage.class, message -> receivedElectionMessage(message))
                            .match(ElectedMessage.class, message -> receivedElectedMessage(message))
                            .match(RingTestMessage.class, message -> receivedRingTestMessage(message))
                            .match(InitializeRingMessage.class, message -> initializeRing(message))
                            .build();
    }

    /**
     * Lorsqu'il recoit le message de debut d'election l'acteur envoie un message d'election à son voisin
     */
    private void receivedStartElectionMessage(){
        System.out.println("Démarrage de l'élection");
        System.out.println("Processus n°"+this.rank+" envoie message d'election à son voisin");
        ElectionMessage electionMessage = new ElectionMessage(this.UID);
        this.nextActor.forward(electionMessage, getContext());
    }
    /**
     * Comportement d'un acteur s'il recoit un message d'election
     * @param message 
     */
    private void receivedElectionMessage(ElectionMessage message){
        //Si l'UID du message est supérieur à l'UID de l'acteur, il devient participant et forward le message
        if(message.UID>this.UID){
            System.out.println("ELECTION : UID reçu supérieur à l'IUD du process n°"+this.rank+" UID " +this.UID+" Le process devient participant et forward le message");
            this.participant=true;
            //FORWARD to next actor
            this.nextActor.forward(message,getContext());
        }
        //sinon si l'UID du message est inférieur a l'UID de l'acteur et qu'il n'est pas participant, il devient participant, remplace l'UID du message par le sien et forward le message
        else if(message.UID<this.UID && !this.participant){
            System.out.println("ELECTION : UID reçu inférieur à l'IUD du process n°"+this.rank+" UID " +this.UID+" Le process est non participant message envoyé au voisin en remplaçant l'IUD");
            message.UID=this.UID;
            this.participant=true;
            //SEND to next actor
            this.nextActor.forward(message,getContext());
        }
        //sinon si l'UID et inférieur et qu'il est participant, il ne fait rien
        else if(message.UID<this.UID && this.participant){
            System.out.println("ELECTION : UID reçu inférieur à l'IUD du process n°"+this.rank+" UID " +this.UID+" Le process est participant message supprimé");

        }
        //sinon si l'IUD du message est égale à l'UID de l'acteur il devient leader, n'est plus participant et envoie un message elu à son voisin avec son UID
        else if(message.UID==this.UID){
            System.out.println("ELECTION : UID reçu égal à l'IUD du process n°"+this.rank+" UID " +this.UID+" Il est nommé leader et envoi un message elu à son voisin");
            this.leader=true;
            this.participant=false;
            //SEND elected message
            ElectedMessage electedMessage = new ElectedMessage(this.UID);
            this.nextActor.forward(electedMessage, getContext());
        }
    }

    /**
     * Comportement d'un acteur s'il recoit un message elu
     * @param message 
     */
    private void receivedElectedMessage(ElectedMessage message){
        //si l'UID du message est différent de l'UID de l'acteur, il n'est plus participant son leaderUID devient l'UID du message et il forward le messag elu au prochain acteur
        if(message.UID!=this.UID){
            this.participant=false;
            this.leaderUID=message.UID;
            this.nextActor.forward(message,getContext());
        }
        //sinon son leaderUID devient celui du message et il envoie un message à l'acteur initial
        else{
            this.leaderUID=message.UID;
            getSender().tell("ELU : IUD du message égal à celui du processus n°"+this.rank+". Fin de la transmission message élu", this.getSelf());
        }
    }
    
    /**
     * Comportement lors de la reception du message de test
     * @param message 
     */
    private void receivedRingTestMessage(RingTestMessage message){
        
        if(!message.text.equals("")&&this.rank==1){
            getSender().tell("Message obtenu a la fin de l'anneau :\n"+message.text, this.getSelf());
        }
        else if(this.rank==1){
            String text = "\nprocess n°"+this.rank+" UID :"+this.UID;
            message.text=text;
            this.nextActor.forward(message, getContext());
        }
        else{
            String text = "\nprocess n°"+this.rank+" UID :"+this.UID;
            message.text+=text;
            this.nextActor.forward(message, getContext());
        }
    }
    
    /**
     * Initialise l'anneau
     * @param message 
     */
    private void initializeRing(InitializeRingMessage message){        
            int nextRank = 0;
            if(message.rank==1){
                System.out.println("Début de la construction de l'anneau");
            }
            if(message.rank==message.nbProcess){
                nextRank = 1;
                this.rank = message.rank;
                this.UID = message.UIDList.get(message.UIDList.size()-1);
                message.UIDList.remove(message.UIDList.size()-1);
                this.nextActor=message.initiatorActor;
                getSender().tell("Fin de la construction de l'anneau", this.getSelf());
            }
            else{
                nextRank = message.rank+1;
                this.rank = message.rank;
                this.UID = message.UIDList.get(message.UIDList.size()-1);
                message.UIDList.remove(message.UIDList.size()-1);
                String nextActorName = "process"+nextRank;
                this.nextActor = getContext().actorOf(ProcessActor.props(), nextActorName);
                
                message.rank++;
                nextActor.forward(message, getContext());
            }            
                        
    }

    // Méthode servant à la création d'un acteur
    public static Props props() {
            return Props.create(ProcessActor.class);
    }

    // Définition des messages en inner classes
    public interface Message {}

    public static class StartElectionMessage{
        public StartElectionMessage(){
            
        }
    }
    /**
     * Message d'election
     */
    private static class ElectionMessage implements Message {
            public int UID;
            public ElectionMessage(int UID) {
                this.UID=UID;
            }
    }	

    /**
     * Message d'elu
     */
    private static class ElectedMessage implements Message {
            public int UID;
            public ElectedMessage(int UID) {
                this.UID=UID;
            }
    }
    
    public static class RingTestMessage implements Message{
        protected String text="";
        public RingTestMessage(){
        }
    }
    
    /**
     * Message pour intialiser l'anneau
     * Contient le rang du prochain processus dans l'anneau et le nombre de processus dans l'anneau
     */
    public static class InitializeRingMessage implements Message{
        private int rank=1;
        public int nbProcess;
        public List<Integer> UIDList;
        public ActorRef initiatorActor;
        public InitializeRingMessage(int nbProcess, List<Integer> UIDList,ActorRef initiatorActor){
            this.nbProcess=nbProcess;
            this.UIDList=UIDList;
            this.initiatorActor=initiatorActor;
        }
    }
    
}
