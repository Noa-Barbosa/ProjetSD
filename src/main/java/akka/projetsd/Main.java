/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package akka.projetsd;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.projetsd.actor.HelloWorldActor;
/**
 *
 * @author noaba
 */
public class Main {

    public static void main(String[] args) {
        ActorSystem actorSystem = ActorSystem.create();
        ActorRef helloActor = actorSystem.actorOf(HelloWorldActor.props());
        // Envoi de messages simples
        helloActor.tell(new HelloWorldActor.SayHello("Akka"), ActorRef.noSender());
        helloActor.tell(new HelloWorldActor.SayBye(), ActorRef.noSender());
        
        // Arrêt du système d'acteurs
        actorSystem.terminate();
    }
}
