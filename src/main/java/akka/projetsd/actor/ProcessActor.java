/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package akka.projetsd.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import java.util.Random;

/**
 * Acteur symbolisant un processus
 * @author noaba
 */
public class ProcessActor extends AbstractActor{
    
    /**
     * Si le processus participe a une reunion ou pas (faux par défaut)
     */
    private boolean participant;
    /**
     * Si le processus est leader ou pas (faux par défaut)
     */
    private boolean leader;
    /**
     * UID du processus (généré aléatoirement entre 1 et 30)
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
        Random random = new Random();
        this.UID = random.nextInt(30) + 1;
        this.leaderUID=-1;
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
    
}
