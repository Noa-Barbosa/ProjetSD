/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package akka.projetsd.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 5 * hash + (this.participant ? 1 : 0);
        hash = 5 * hash + (this.leader ? 1 : 0);
        hash = 5 * hash + this.rank;
        hash = 5 * hash + Objects.hashCode(this.nextActor);

        Random random = new Random();
        int randomInt1 = random.nextInt(5) + 1;
        int randomInt2 = random.nextInt(5) + 1;
        return hash/randomInt1*randomInt2;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProcessActor other = (ProcessActor) obj;
        if (this.participant != other.participant) {
            return false;
        }
        if (this.leader != other.leader) {
            return false;
        }
        if (this.rank != other.rank) {
            return false;
        }
        if (this.UID != other.UID) {
            return false;
        }
        if (this.leaderUID != other.leaderUID) {
            return false;
        }
        return Objects.equals(this.nextActor, other.nextActor);
    }
    
    

    // Méthode servant à déterminer le comportement de l'acteur lorsqu'il reçoit un message
    @Override
    public AbstractActor.Receive createReceive() {
            return receiveBuilder()
                            .match(StartElectionMessage.class, message -> receivedStartElectionMessage(message))
                            .match(ElectionMessage.class, message -> receivedElectionMessage(message))
                            .match(ElectedMessage.class, message -> receivedElectedMessage(message))
                            .match(RingTestMessage.class, message -> receivedRingTestMessage(message))
                            .match(InitializeRingMessage.class, message -> initializeRing(message))
                            .build();
    }

    private void receivedStartElectionMessage(StartElectionMessage message){
        System.out.println("Démarrage de l'élection");
        System.out.println("Le processus n°"+this.rank+" envoie message d'election à son voisin");
        ElectionMessage electionMessage = new ElectionMessage(this.UID);
        this.nextActor.tell(electionMessage, getSelf());
    }
    /**
     * Comportement d'un acteur s'il recoit un message d'election
     * @param message 
     */
    private void receivedElectionMessage(ElectionMessage message){
        if(message.UID>this.UID){
            System.out.println("ELECTION : UID reçu supérieur à l'IUD du process n°"+this.rank+" UID " +this.UID+" Le process devient participant et forward le message");
            this.participant=true;
            //FORWARD to next actor
            this.nextActor.forward(message,getContext());
        }
        else if(message.UID<this.UID && !this.participant){
            System.out.println("ELECTION : UID reçu inférieur à l'IUD du process n°"+this.rank+" UID " +this.UID+" Le process est non participant message envoyé au voisin en remplaçant l'IUD");
            message.UID=this.UID;
            this.participant=true;
            //SEND to next actor
            this.nextActor.tell(message,getSelf());
        }
        else if(message.UID<this.UID && this.participant){
            //DISCARD MESSAGE
            System.out.println("ELECTION : UID reçu inférieur à l'IUD du process n°"+this.rank+" UID " +this.UID+" Le process est participant message supprimé");

        }
        else if(message.UID==this.UID){
            System.out.println("ELECTION : UID reçu égal à l'IUD du process n°"+this.rank+" UID " +this.UID+" Il est nommé leader et envoi un message elu à son voisin");
            this.leader=true;
            this.participant=false;
            message.UID=this.UID;
            //SEND elected message
            ElectedMessage electedMessage = new ElectedMessage(this.UID);
            this.nextActor.tell(electedMessage,getSelf());
        }
    }

    /**
     * Comportement d'un acteur s'il recoit un message elu
     * @param message 
     */
    private void receivedElectedMessage(ElectedMessage message){
        if(message.UID!=this.UID){
            System.out.println("ELU : IUD du message différent du processus n°"+this.rank+". Le leader est l'UID : "+message.UID);
            this.participant=false;
            this.leaderUID=message.UID;
            //FORWARD elected message
            this.nextActor.forward(message,getContext());
        }
        else{
            System.out.println("ELU : IUD du message égal à celui du processus n°"+this.rank+". Fin de la transmission message élu");
            this.leaderUID=message.UID;
        }
    }
    
    private void receivedRingTestMessage(RingTestMessage message){
        
        if(!message.text.equals("test")&&this.rank==1){
            System.out.println("Message obtenu a la fin de l'anneau :\n"+message.text);
        }
        else if(this.rank==1){
            System.out.println("Message au début de l'anneau :\n"+message.text);
            String text = "\nprocess n°"+this.rank+" UID :"+this.UID;
            message.text+=text;
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
            if(message.rank>message.nbProcess){
                System.out.println("Fin de la construction de l'anneau");
            }
            else if(message.rank==message.nbProcess){
                nextRank = 1;
                this.rank = message.rank;
                this.UID = this.hashCode();               
                this.nextActor=getSender();
                message.rank++;
                nextActor.forward(message, getContext());
            }
            else{
                nextRank = message.rank+1;
                this.rank = message.rank;
                this.UID = this.hashCode();
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
        protected String text="test";
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
        public InitializeRingMessage(int nbProcess){
            this.nbProcess=nbProcess;
        }
    }
    
}
