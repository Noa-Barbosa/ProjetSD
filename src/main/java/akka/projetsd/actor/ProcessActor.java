/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package akka.projetsd.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import java.util.Objects;

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
     * Reference du processus précédent dans l'anneau (null par défaut)
     */
    private ActorRef previousActor;
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
        this.previousActor = null;
        this.UID = -1;
        this.leaderUID=-1;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.participant ? 1 : 0);
        hash = 89 * hash + (this.leader ? 1 : 0);
        hash = 89 * hash + this.rank;
        hash = 89 * hash + Objects.hashCode(this.nextActor);
        hash = 89 * hash + Objects.hashCode(this.previousActor);
        hash = 89 * hash + this.UID;
        hash = 89 * hash + this.leaderUID;
        return hash;
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
        if (!Objects.equals(this.nextActor, other.nextActor)) {
            return false;
        }
        return Objects.equals(this.previousActor, other.previousActor);
    }

   
    
    

    // Méthode servant à déterminer le comportement de l'acteur lorsqu'il reçoit un message
    @Override
    public AbstractActor.Receive createReceive() {
            return receiveBuilder()
                            .match(ElectionMessage.class, message -> processElectionMessage(message))
                            .match(ElectedMessage.class, message -> processElectedMessage(message))
                            .build();
    }

    /**
     * Comportement d'un acteur s'il recoit un message d'election
     * @param message 
     */
    private void processElectionMessage(ElectionMessage message){
        if(message.UID>this.UID){
            this.participant=true;
            //FORWARD to next actor
        }
        else if(message.UID<this.UID && !this.participant){
            message.UID=this.UID;
            this.participant=true;
            //SEND to next actor
        }
        else if(message.UID<this.UID && this.participant){
            //DISCARD MESSAGE
        }
        else if(message.UID==this.UID){
            this.leader=true;
            this.participant=false;
            ElectedMessage electedMessage = new ElectedMessage(this.UID);
            //SEND elected message
        }
    }

    /**
     * Comportement d'un acteur s'il recoit un message elu
     * @param message 
     */
    private void processElectedMessage(ElectedMessage message){
        if(message.UID!=this.UID){
            this.participant=false;
            this.leaderUID=message.UID;
            //FORWARD elected message
        }
        else{
            this.leaderUID=message.UID;
        }
    }
    
    private void initializeRing(InitializeRingMessage message){        
        
            if(message.rank>message.nbProcess){
                System.out.println("Ring construit");
            }
            else if(message.rank==1){
                this.nextActor = null; //recupere l'actor numero rank+1 depuis la liste des acteurs
                this.previousActor = null; //recupere l'actor numero nbProcess depuis la liste des acteurs
            }
            else if(message.rank==message.nbProcess){
                this.nextActor = null; //recuperer l'actor numero 1 depuis la liste des acteurs crées
                this.previousActor = null; //recuperer l'actor rank-1 
            }
            else{
                this.nextActor = null; //recupere l'actor rank+1 depuis la liste des acteurs
                this.previousActor = null; //recupere l'actor rank-1 depuis la liste des acteurs
            }
        
            this.rank = message.rank;
            this.UID = this.hashCode();
            message.rank++;
            //FORWARD le message au nextActor      
        
        
    }

    // Méthode servant à la création d'un acteur
    public static Props props() {
            return Props.create(ProcessActor.class);
    }

    // Définition des messages en inner classes
    public interface Message {}

    /**
     * Message d'election
     */
    public static class ElectionMessage implements Message {
            public int UID;
            public ElectionMessage(int UID) {
                this.UID=UID;
            }
    }	

    /**
     * Message d'elu
     */
    public static class ElectedMessage implements Message {
            public int UID;
            public ElectedMessage(int UID) {
                this.UID=UID;
            }
    }
    
    /**
     * Message pour intialiser l'anneau
     * Contient le rang du prochain processus dans l'anneau et le nombre de processus dans l'anneau
     */
    public static class InitializeRingMessage implements Message{
        public int rank;
        public int nbProcess;
        public InitializeRingMessage(int rank, int nbProcess){
            this.rank=rank;
            this.nbProcess=nbProcess;
        }
    }
    
}
