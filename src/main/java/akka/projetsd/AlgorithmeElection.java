/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package akka.projetsd;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.projetsd.actor.ProcessActor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

/**
 * Classe permettant de lancer un algorithme d'election
 * @author noaba
 */
public class AlgorithmeElection {
    
    /**
     * Lance un systeme d'acteur qui execute un exemple d'algorithme d'election
     * @param nbProcess le nombre de processus dans l'anneau
     */
    public static void runElection(int nbProcess){
        //Liste des UIDs des acteurs
        List<Integer> UIDList = generateUIDList(nbProcess);
        
        //Creation du système d'acteur et de l'acteur initiateur des messages dans l'anneau
        ActorSystem actorSystem = ActorSystem.create();       
        ActorRef initiatorActor = actorSystem.actorOf(ProcessActor.props(), "process1");
        
        //Initialisation de l'anneau
        CompletionStage<Object> initialiazeResult = Patterns.ask(initiatorActor, new ProcessActor.InitializeRingMessage(nbProcess,UIDList,initiatorActor), Duration.ofSeconds(40));
        try {
                var resultat =initialiazeResult.toCompletableFuture().get();
                System.out.println(resultat);
        } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
        }
        
        CompletionStage<Object> ringTestResult = Patterns.ask(initiatorActor, new ProcessActor.RingTestMessage(), Duration.ofSeconds(40));
        try {
                var resultat=ringTestResult.toCompletableFuture().get();
                System.out.println(resultat);
        } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
        }
        
        //Envoie du message de début d'élection
        CompletionStage<Object> electionResult = Patterns.ask(initiatorActor, new ProcessActor.StartElectionMessage(), Duration.ofSeconds(40));
        try {
                var resultat=electionResult.toCompletableFuture().get();
                System.out.println(resultat);
        } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
        }
        // Arrêt du système d'acteurs
        actorSystem.terminate();
    }
    
    /**
     * Genere une liste d'entier pour les UIDs des processus
     * @param nbProcess nombre de processus
     * @return une liste d'entier unique
     */
    public static List<Integer> generateUIDList(int nbProcess){
        Set<Integer> generatedNumbers = new HashSet<>();
        Random random = new Random();

        while (generatedNumbers.size() < nbProcess) {
            int randomInt = random.nextInt(nbProcess*10);

            // Check if the number is not already generated
            generatedNumbers.add(randomInt);
              
        }

        return new ArrayList(generatedNumbers);
    }
    
    public static void main(String[] args) {
        runElection(10);
    }
    
    
    
}
