package com.yube.sorters;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.compat.java8.FutureConverters;
import scala.reflect.ClassTag;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ActorBasedMergeSorter implements Sorter {

    private static final Timeout ACTOR_REPLY_TIMEOUT = Timeout.apply(5, TimeUnit.SECONDS);

    private final int splitThreshold;
    private final ActorSystem actorSystem;

    private int runId = 0;

    public ActorBasedMergeSorter(int splitThreshold, ActorSystem actorSystem) {
        this.splitThreshold = splitThreshold;
        this.actorSystem = actorSystem;
    }

    @Override
    public String getAlgorithmName() {
        return String.format("Actor Based Merge Sort With %d-Split Threshold", splitThreshold);
    }

    @Override
    public void sort(Comparable[] a) {
        runId++;
        ActorRef sorterActor = actorSystem.actorOf(SorterActor.props(runId, splitThreshold).withDispatcher("akka.actor.sorting-dispatcher"), "main-sorter-" + runId);
        SorterActor.SortingChunk sortingChunk = new SorterActor.SortingChunk(a);
        ClassTag<Comparable[]> comparableArrayClassTag = scala.reflect.ClassTag$.MODULE$.apply(Comparable[].class);
        CompletableFuture<Comparable[]> sortedArrayFuture =
                FutureConverters.toJava(Patterns.ask(sorterActor, sortingChunk, ACTOR_REPLY_TIMEOUT)
                        .mapTo(comparableArrayClassTag)).toCompletableFuture();
        try {
            Comparable[] result = sortedArrayFuture.get();
            System.arraycopy(result, 0, a, 0, a.length);
            sorterActor.tell(PoisonPill.getInstance(), ActorRef.noSender());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
