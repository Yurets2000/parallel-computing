package com.yube.sorters;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import lombok.Data;
import scala.compat.java8.FutureConverters;
import scala.reflect.ClassTag;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public class SorterActor extends AbstractActor {

    private static final int ARRAY_CHUNK_SIZE_THRESHOLD = 16;
    private static final Timeout ACTOR_RESPONSE_TIMEOUT = Timeout.apply(5, TimeUnit.SECONDS);

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final int runId;
    private final int splitThreshold;

    public SorterActor(int runId, int splitThreshold) {
        this.runId = runId;
        this.splitThreshold = splitThreshold;
    }

    public static Props props(int runId, int splitThreshold) {
        return Props.create(SorterActor.class, runId, splitThreshold);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(SortingChunk.class, sortingChunk -> {
            int n = sortingChunk.arrayChunk.length;
            // log.info("Sorting array with length {}", n);
            if (sortingChunk.splitCount < splitThreshold && sortingChunk.arrayChunk.length > ARRAY_CHUNK_SIZE_THRESHOLD) {
                // Split sorting chunk on two sub-chunks for child actors processing
                int m = n / 2;
                Comparable[] arrayChunkLeft = new Comparable[m];
                Comparable[] arrayChunkRight = new Comparable[n - m];
                System.arraycopy(sortingChunk.arrayChunk, 0, arrayChunkLeft, 0, arrayChunkLeft.length);
                System.arraycopy(sortingChunk.arrayChunk, m, arrayChunkRight, 0, arrayChunkRight.length);
                SortingChunk sortingChunkLeft = new SortingChunk(sortingChunk.splitCount + 1, arrayChunkLeft);
                SortingChunk sortingChunkRight = new SortingChunk(sortingChunk.splitCount + 1, arrayChunkRight);
                // Create child sorter actors
                ActorRef sorterLeft = getContext().actorOf(SorterActor.props(runId, splitThreshold), "sorter-" + runId + "-left-" + sortingChunkLeft.splitCount);
                ActorRef sorterRight = getContext().actorOf(SorterActor.props(runId, splitThreshold), "sorter-" + runId + "-right-" + sortingChunkRight.splitCount);
                // Get future results of child sorter actors, merge them and pipe result to sender
                ClassTag<Comparable[]> comparableArrayClassTag = scala.reflect.ClassTag$.MODULE$.apply(Comparable[].class);
                CompletableFuture<Comparable[]> sortedArrayChunkLeftFuture =
                        FutureConverters.toJava(Patterns.ask(sorterLeft, sortingChunkLeft, ACTOR_RESPONSE_TIMEOUT)
                                .mapTo(comparableArrayClassTag)).toCompletableFuture();
                CompletableFuture<Comparable[]> sortedArrayChunkRightFuture =
                        FutureConverters.toJava(Patterns.ask(sorterRight, sortingChunkRight, ACTOR_RESPONSE_TIMEOUT)
                                .mapTo(comparableArrayClassTag)).toCompletableFuture();
                BiFunction<Comparable[], Comparable[], Comparable[]> mergeFunction = MergeSorter::merge;
                CompletableFuture<Comparable[]> sortedArrayChunkFuture =
                        sortedArrayChunkLeftFuture.thenCombine(sortedArrayChunkRightFuture, mergeFunction);
                Patterns.pipe(FutureConverters.toScala(sortedArrayChunkFuture), getContext().dispatcher()).to(getSender());
                // Terminate child sorter actors
                // sorterLeft.tell(PoisonPill.getInstance(), getSelf());
                // sorterRight.tell(PoisonPill.getInstance(), getSelf());
            } else {
                // Sort sorting chunk
                Comparable[] sortedArrayChunk = new Comparable[n];
                System.arraycopy(sortingChunk.arrayChunk, 0, sortedArrayChunk, 0, n);
                MergeSorter.sort(sortedArrayChunk);
                getSender().tell(sortedArrayChunk, getSelf());
            }
        }).build();
    }

    @Data
    public static final class SortingChunk {

        int splitCount;
        Comparable[] arrayChunk;

        public SortingChunk(Comparable[] arrayChunk) {
            this.splitCount = 0;
            this.arrayChunk = arrayChunk;
        }

        public SortingChunk(int splitCount, Comparable[] arrayChunk) {
            this.splitCount = splitCount;
            this.arrayChunk = arrayChunk;
        }
    }
}
