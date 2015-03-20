/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track.bigwig;

import org.mskcc.shenkers.control.track.bam.*;
import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.LockSupport;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BigWigIterator;
import org.broad.igv.bbfile.WigItem;
import org.fxmisc.easybind.EasyBind;
import org.mskcc.shenkers.control.track.View;
import static org.mskcc.shenkers.control.track.bam.BamView1.coverage;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import org.mskcc.shenkers.view.LineHistogramView;
import org.mskcc.shenkers.view.SparseLineHistogramView;

/**
 *
 * @author sol
 */
public class BigWigView implements View<BigWigContext> {

    private final Logger logger = LogManager.getLogger();

    @Override
    public Task<Pane> getContent(BigWigContext context) {
        return context.spanProperty().getValue().map(i -> {

            String chr = i.getChr();
            int start = i.getStart();
            int end = i.getEnd();

            Task<Pane> task = new PaneTask(context, chr, start, end);

            logger.info("returning task");
            return task;
        }
        ).orElse(
                new Task<Pane>() {

                    @Override
                    protected Pane call() throws Exception {
                        return new BorderPane(new Label("coordinates not set"));
                    }
                }
        //                new BorderPane(new Label("bamview1: span not set"))
        //                new Pane()
        );
    }

    public class PaneTask extends Task<Pane> {

        BigWigContext context;
        BBFileReader bbfr;
        String chr;
        int start;
        int end;
        BooleanBinding flipBinding;
        Semaphore semaphore;

        public PaneTask(BigWigContext context, String chr, int start, int end) {
            super();
            this.context = context;
            this.bbfr = context.readerProperty().getValue();
            this.chr = chr;
            this.start = start;
            this.end = end;
        }
        
        @Override
        protected Pane call() throws Exception {
            SAMRecordIterator sri = null;
            try {
                logger.info("calculating coverage for region {}:{}-{}", chr, start, end);

                logger.info("allocating coverage map");
                Map<Integer, Double> data = new HashMap<>();

                logger.info("acquiring semaphore for bigwig reader");
                context.acquireReader();
                logger.info("parsing bigwig file");

                BigWigIterator bwi = bbfr.getBigWigIterator(chr, start - 1, chr, end, false);
                while (bwi.hasNext()) {
                    if (isCancelled()) {
                        logger.info(Thread.currentThread().getName() + " recieved cancel and terminating");
                        break;
                    }

                    LockSupport.parkNanos(1);

                    WigItem wi = bwi.next();
                    double value = wi.getWigValue();
                    if(value>0)
                    for (int i = Math.max(start, wi.getStartBase() + 1); i <= Math.min(end, wi.getEndBase()); i++) {
                        data.put(i-start, value);
                    }
                }


//            double[] data = ArrayUtils.toPrimitive(IntStream.of(cov).mapToDouble(j -> j + 0.).boxed().collect(Collectors.toList()).toArray(new Double[0]));
                logger.info("setting data");
                SparseLineHistogramView lhv = new SparseLineHistogramView();

                class FlipBinding extends BooleanBinding {

                    private final Property<Optional<GenomeSpan>> span;

                    private FlipBinding(Property<Optional<GenomeSpan>> spanProperty) {
                        this.span = spanProperty;
                    }

                    protected boolean computeValue() {

                        if (span.getValue().isPresent()) {
                            return span.getValue().get().isNegativeStrand();
                        } else {
                            return false;
                        }
                    }

                }

                flipBinding = new FlipBinding(context.spanProperty());

                lhv.flipDomainProperty().bind(flipBinding);
                lhv.setData(data, end - start + 1, this);

                logger.info("calculating max");
                data.values().stream().max((Double d1, Double d2)
                        -> Double.compare(d1, d2)
                ).ifPresent(m -> lhv.setMax(m));
//            IntStream.of(cov).max().ifPresent(m -> lhv.setMax(m));

                logger.info("returning graphic");

                return lhv.getGraphic();
            } catch (Throwable t) {
                logger.info("task {} threw exception {}", this, t.getMessage());
                logger.info("exception: ", t);
                throw t;
            } finally {

                if (sri != null) {
                    sri.close();
                }
                logger.info("{} releasing semaphore for task {}", Thread.currentThread().getName(), this);
                context.releaseReader();
                logger.info("released bigwig reader semaphore");
            }
        }

        @Override
        protected void cancelled() {
            super.cancelled();
            logger.info("Cancelling task");
        }

    }

}
