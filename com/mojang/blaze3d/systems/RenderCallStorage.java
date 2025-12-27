package com.mojang.blaze3d.systems;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RenderCallStorage {
   private final List recordingQueues = ImmutableList.of(new ConcurrentLinkedQueue(), new ConcurrentLinkedQueue(), new ConcurrentLinkedQueue(), new ConcurrentLinkedQueue());
   private volatile boolean recording;
   private volatile int recordingIndex;
   private volatile boolean processing;
   private volatile int processingIndex;
   private volatile int lastProcessedIndex;

   public RenderCallStorage() {
      this.recordingIndex = this.processingIndex = this.lastProcessedIndex + 1;
   }

   public boolean canRecord() {
      return !this.recording && this.recordingIndex == this.processingIndex;
   }

   public boolean startRecording() {
      if (this.recording) {
         throw new RuntimeException("ALREADY RECORDING !!!");
      } else if (this.canRecord()) {
         this.recordingIndex = (this.processingIndex + 1) % this.recordingQueues.size();
         this.recording = true;
         return true;
      } else {
         return false;
      }
   }

   public void record(RenderCall call) {
      if (!this.recording) {
         throw new RuntimeException("NOT RECORDING !!!");
      } else {
         ConcurrentLinkedQueue<RenderCall> concurrentLinkedQueue = this.getRecordingQueue();
         concurrentLinkedQueue.add(call);
      }
   }

   public void stopRecording() {
      if (this.recording) {
         this.recording = false;
      } else {
         throw new RuntimeException("NOT RECORDING !!!");
      }
   }

   public boolean canProcess() {
      return !this.processing && this.recordingIndex != this.processingIndex;
   }

   public boolean startProcessing() {
      if (this.processing) {
         throw new RuntimeException("ALREADY PROCESSING !!!");
      } else if (this.canProcess()) {
         this.processing = true;
         return true;
      } else {
         return false;
      }
   }

   public void process() {
      if (!this.processing) {
         throw new RuntimeException("NOT PROCESSING !!!");
      }
   }

   public void stopProcessing() {
      if (this.processing) {
         this.processing = false;
         this.lastProcessedIndex = this.processingIndex;
         this.processingIndex = this.recordingIndex;
      } else {
         throw new RuntimeException("NOT PROCESSING !!!");
      }
   }

   public ConcurrentLinkedQueue getLastProcessedQueue() {
      return (ConcurrentLinkedQueue)this.recordingQueues.get(this.lastProcessedIndex);
   }

   public ConcurrentLinkedQueue getRecordingQueue() {
      return (ConcurrentLinkedQueue)this.recordingQueues.get(this.recordingIndex);
   }

   public ConcurrentLinkedQueue getProcessingQueue() {
      return (ConcurrentLinkedQueue)this.recordingQueues.get(this.processingIndex);
   }
}
