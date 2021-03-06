/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 *
 * @author sol
 */
public class VerticalOverlay {

    GridPane internal;

    BooleanProperty flipProperty;
    List<Node> nodes;
    
    public VerticalOverlay() {
        internal = new GridPane();
        flipProperty = new SimpleBooleanProperty(false);
        nodes = new ArrayList<>();
        flipProperty.addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
               populateGridPane();
            }
        });
    }
    
    public Pane getContent(){
        return internal;
    }
    
    public void setChildren(List<Node> nodes){
        this.nodes = nodes;
        populateGridPane();
    }
    
    public void flip(){
        flipProperty.set(!flipProperty.get());
    }
    
    private void populateGridPane(){
        ObservableList<Node> children = internal.getChildren();
        children.clear();
        int l = nodes.size();
        boolean flip = flipProperty.get();
       
        for(int i=0; i<l; i++){
            Node n = nodes.get(i);
            GridPane.setHalignment(n, HPos.CENTER);
            GridPane.setValignment(n, VPos.CENTER);
            GridPane.setHgrow(n, Priority.ALWAYS);
            GridPane.setVgrow(n, Priority.ALWAYS);
            GridPane.setRowIndex(n, 0);
            GridPane.setColumnIndex(n, flip ? l-i-1 : i);
            children.add(n);
        }        
    }

}
