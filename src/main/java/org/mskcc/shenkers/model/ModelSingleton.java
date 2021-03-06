/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.model;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import org.mskcc.shenkers.model.datatypes.Genome;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Optional;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.control.track.AbstractContext;
import org.mskcc.shenkers.control.track.Track;
import org.mskcc.shenkers.control.alignment.AlignmentSource;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import org.reactfx.EventSource;

/**
 *
 * @author sol
 */
@Singleton
public class ModelSingleton {

    private static final Logger logger = LogManager.getLogger();

    static ModelSingleton instance;
    
    @Inject
    ModelSingleton() {
        logger.info("creating model singleton");
        genomes = FXCollections.observableArrayList();
        tracks = FXCollections.observableHashMap();
        nTracks = 0;
        spans = FXCollections.observableHashMap();
        alignments = FXCollections.observableHashMap();
        instance = this;
    }
    
    public static ModelSingleton getInstance(){
        if(instance==null){
            return new ModelSingleton();
        }
        return instance;
    }

    ObservableList<Genome> genomes;
    ObservableMap<Genome, ObservableList<Track<AbstractContext>>> tracks;
    ObservableMap<Genome, Property<Optional<GenomeSpan>>> spans;
    Map<Pair<Genome, Genome>, AlignmentSource> alignments;

    private int nTracks;
    
    
    EventSource<CoordinateChangeEvent> coordinateChangeEvents;
    
    @Inject
    private void setCoordinateSource(@CoordinateChange EventSource<CoordinateChangeEvent> source){
        this.coordinateChangeEvents = source;
    }

    public ObservableValue<Optional<GenomeSpan>> getSpan(Genome g) {
        return spans.get(g);
    }

    public void setAlignments(List<Pair<Genome, Genome>> genomePairs, List<AlignmentSource> alignmentSources) {
        for (int i = 0; i < genomePairs.size(); i++) {
            alignments.put(genomePairs.get(i), alignmentSources.get(i));
        }
    }

    public Map<Pair<Genome, Genome>, AlignmentSource> getAlignments() {
        return alignments;
    }


    public void setSpan(Genome g, Optional<GenomeSpan> span) {
        logger.info("genome {}", g);
        logger.info("spans.get(g) {}", spans.get(g));
        logger.info("span {}", span);
        CoordinateChangeEvent event = new CoordinateChangeEvent(g, spans.get(g).getValue(), span);
        
        this.spans.get(g).setValue(span);
        coordinateChangeEvents.push(event);
    }

    public void addTrack(Genome g, Track track) {
        tracks.get(g).add(track);
        track.getSpan().bind(getSpan(g));
        setnTracks(getnTracks() + 1);
    }

    public void removeTrack(Genome g, Track track) {
        tracks.get(g).remove(track);
        setnTracks(getnTracks() - 1);
    }

    public ObservableList<Genome> getGenomes() {
        return genomes;
    }

    public void addGenome(Genome g) {
        tracks.put(g, FXCollections.observableArrayList());
        spans.put(g, new SimpleObjectProperty<>(Optional.empty()));
        genomes.add(g);
    }

    public void removeTrack(Genome g) {
        tracks.remove(g);
        genomes.remove(g);
    }

    /**
     * @return the nTracks
     */
    public int getnTracks() {
        return nTracks;
    }

    /**
     * @param nTracks the nTracks to set
     */
    public void setnTracks(int nTracks) {
        this.nTracks = nTracks;
    }

    public ObservableList<Track<AbstractContext>> getTracks(Genome g) {
        return tracks.get(g);
    }
    
    public ObservableMap<Genome,ObservableList<Track<AbstractContext>>> getTracks() {
        return tracks;
    }

    public ObservableMap<Genome, Property<Optional<GenomeSpan>>> getObservableSpans() {
        return FXCollections.unmodifiableObservableMap(spans);
    }

}
