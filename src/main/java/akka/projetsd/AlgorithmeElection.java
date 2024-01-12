/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package akka.projetsd;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.projetsd.actor.ProcessActor;

/**
 *
 * @author noaba
 */
public class AlgorithmeElection {
    
    public static void runElection(int nbProcess){
        ActorSystem actorSystem = ActorSystem.create();       
        ActorRef firstProcess = actorSystem.actorOf(ProcessActor.props(), "process1");
        firstProcess.tell(new ProcessActor.InitializeRingMessage(nbProcess), firstProcess);
        firstProcess.tell(new ProcessActor.RingTestMessage(), firstProcess);
        firstProcess.tell(new ProcessActor.StartElectionMessage(), firstProcess);
        
        // Arrêt du système d'acteurs
        actorSystem.terminate();
    }
    
    public static void main(String[] args) {
        runElection(10);
    }
    
}
