package org.mskcc.shenkers.controller;

import com.google.inject.Inject;
import com.sun.javafx.binding.ContentBinding;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ListBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.easybind.EasyBind;
import org.mskcc.shenkers.control.track.FileType;
import org.mskcc.shenkers.control.track.TrackBuilder;
import org.mskcc.shenkers.control.track.Track;
import org.mskcc.shenkers.control.track.View;
import org.mskcc.shenkers.control.track.bam.BamContext;
import org.mskcc.shenkers.model.ModelSingleton;
import org.mskcc.shenkers.model.SimpleTrack;
import org.mskcc.shenkers.model.datatypes.Genome;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;
import org.mskcc.shenkers.view.LineHistogramView;

public class FXMLController implements Initializable {

    private static final Logger logger = LogManager.getLogger();

    ModelSingleton model = ModelSingleton.getInstance();

    @FXML
    Pane histogram;

    @FXML
    Pane histogram2;

    @FXML
    ListView<String> lv;

    int rowIndex = 0;

    @FXML
    SplitPane sp;

//    @FXML
//    ListView lv1, lv2, lv3;
    @FXML
    Pane canvas;

    @FXML
    BorderPane stp;

    TrackBuilder trackBuilder;

    @Inject
    private void setTrackBuilder(TrackBuilder b) {
        System.out.println("injecting track builder");
        this.trackBuilder = b;
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
    }

    @FXML
    private void handleButtonAction2(ActionEvent event) {
        System.out.println("You clicked me2!");
        Label child = new Label("c=0,r=" + rowIndex);
        Label child2 = new Label("c=1,r=" + rowIndex);

//        gridpane.addRow(rowIndex, child, child2);
////        gridpane.setConstraints(child, 0, rowIndex, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
//        gridpane.getRowConstraints().add(new RowConstraints());
//        gridpane.getRowConstraints().get(rowIndex).setValignment(VPos.TOP);
//        gridpane.getRowConstraints().get(rowIndex).setVgrow(Priority.ALWAYS);
        rowIndex++;
        lv.getItems().add(child2.getText());

        x.set((x.getValue() + 10) % 100);

    }
    SimpleTrack sim;

    @FXML
    private void loadGenome(ActionEvent event) {
        System.out.println("loading genome...");

        GridPane gp1 = new GridPane();
        gp1.add(new Label("Reference sequence (FASTA)"), 0, 0);
        Button selectFasta = new Button("Select");
        TextField selectedFasta = new TextField("");
        gp1.add(selectedFasta, 1, 0);
        gp1.add(selectFasta, 2, 0);

        gp1.add(new Label("Reference sequence ID"), 0, 1);
        TextField genomeID = new TextField("");
        gp1.add(genomeID, 1, 1);

        gp1.add(new Label("Reference sequence name"), 0, 2);
        TextField genomeName = new TextField("");
        gp1.add(genomeName, 1, 2);

        selectFasta.setOnAction(
                actionEvent -> {
                    FileChooser fc = new FileChooser();
                    Stage s = new Stage();
                    fc.setTitle("Select a fasta reference sequence");
                    File selection = fc.showOpenDialog(s);
                    if (selection != null) {
                        System.out.println(selection.getAbsolutePath());
                        selectedFasta.setText(selection.getAbsolutePath());
                    }
                }
        );

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Load genome");
        alert.setHeaderText("Configure new genome");
        alert.getDialogPane().setContent(gp1);

//        ButtonType loginButtonType = new ButtonType("Create genome", ButtonData.FINISH);
//        alert.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
//
//        ((Button)alert.getDialogPane().lookupButton(ButtonData.OK_DONE)).setText("Create genome");
        alert.showAndWait().ifPresent(buttonType -> {
            System.err.println(buttonType);
            System.err.println(buttonType.getText());
            if (buttonType.equals(ButtonType.OK)) {
                System.err.println("finished");
                String fastaFile = selectedFasta.getText();
                String name = genomeName.getText();
                String id = genomeID.getText();
                String text = String.format("%s %s %s", selectedFasta.getText(), genomeID.getText(), genomeName.getText());
                Genome newGenome = new Genome(id, name);
                model.addGenome(newGenome);
            }
            if (buttonType.equals(ButtonType.CANCEL)) {
                System.err.println("Canceled");
            }
        });

    }

    @FXML
    private void loadTrack(ActionEvent event) {
        logger.info("Loading track...");
        System.out.println("loading track...");

        GridPane gp1 = new GridPane();
        gp1.add(new Label("Track file (BAM,BigWig)"), 0, 0);
        Button selectFile = new Button("Select File");
        TextField selectedFile = new TextField("");
        gp1.add(selectedFile, 1, 0);
        gp1.add(selectFile, 2, 0);
        selectFile.setOnAction(
                actionEvent -> {
                    FileChooser fc = new FileChooser();
                    Stage s = new Stage();
                    fc.setTitle("Select a fasta reference sequence");
                    File selection = fc.showOpenDialog(s);
                    if (selection != null) {
                        System.out.println(selection.getAbsolutePath());
                        selectedFile.setText(selection.getAbsolutePath());
                    }
                }
        );

        gp1.add(new Label("Reference sequence ID"), 0, 1);

        ObservableList<Genome> genomes = model.getGenomes();
//                = FXCollections.observableArrayList(
//                        new Genome("dm3", "melanogaster"),
//                        new Genome("dya", "yakuba")
//                );

        ComboBox<Genome> genomeSelector = new ComboBox<>(genomes);

        genomeSelector.setConverter(
                new StringConverter<Genome>() {

                    public String toString(Genome object) {
                        return object.getId();
                    }

                    @Override
                    public Genome fromString(String string) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                }
        );

        gp1.add(genomeSelector, 1, 1);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Load track");
        alert.setHeaderText("Configure new track");
        alert.getDialogPane().setContent(gp1);

//        ButtonType loginButtonType = new ButtonType("Create genome", ButtonData.FINISH);
//        alert.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
//
//        ((Button)alert.getDialogPane().lookupButton(ButtonData.OK_DONE)).setText("Create genome");
        alert.showAndWait().ifPresent(buttonType -> {
            System.err.println(buttonType);
            System.err.println(buttonType.getText());
            if (buttonType.equals(ButtonType.OK)) {
                System.err.println("finished");
//                Track t = new Track() {
//                    public String getName() {
//                        return selectedFile.getText();
//                    }
//                };

                Track<BamContext> t = trackBuilder.load(FileType.BAM, selectedFile.getText());

                Genome selectedGenome = genomeSelector.getSelectionModel().getSelectedItem();

                // bind the span property of this track to the model
                t.getSpan().bind(model.getSpan(selectedGenome));

                t.getSpan().addListener(new ChangeListener<Optional<GenomeSpan>>() {
                    public void changed(ObservableValue<? extends Optional<GenomeSpan>> observable, Optional<GenomeSpan> oldValue, Optional<GenomeSpan> newValue) {
                        logger.info("span change detected in track " + t);
                    }
                });

                model.addTrack(selectedGenome, t);

            }
            if (buttonType.equals(ButtonType.CANCEL)) {
                System.err.println("Canceled");
            }
        });
    }

    IntegerProperty x = new SimpleIntegerProperty(0);
    ObservableList<Node> genomeSplitPaneNodes;
    ListView<String> trackListView;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<Genome> genomes = model.getGenomes();
        trackListView = new ListView<String>();

        {
            LineHistogramView lhv = new LineHistogramView();
            int max = 300;
            List<Double> collect = IntStream.range(0, max).mapToDouble(i -> Math.sin((i + 0.) / (max / 10))).boxed().collect(Collectors.toList());
            double[] data = ArrayUtils.toPrimitive(collect.toArray(new Double[0]));

            lhv.setData(data);
            lhv.widthProperty().bind(histogram.widthProperty());
            lhv.heightProperty().bind(histogram.heightProperty());
            lhv.setMin(-1);
            lhv.setMax(1);

            histogram.getChildren().add(lhv.getGraphic());
        }
        {
            LineHistogramView lhv = new LineHistogramView();
            int max = 300;
            List<Double> collect = IntStream.range(0, max).mapToDouble(i -> Math.sin((i + 0.) / (max / 10))).boxed().collect(Collectors.toList());
            double[] data = ArrayUtils.toPrimitive(collect.toArray(new Double[0]));

            lhv.setData(data);
            lhv.widthProperty().bind(histogram2.widthProperty());
            lhv.heightProperty().bind(histogram2.heightProperty());
            lhv.setMin(-1);
            lhv.setMax(1);
            lhv.setFlipDomain(true);
            lhv.setFlipRange(true);

            histogram2.getChildren().add(lhv.getGraphic());
        }

        System.out.println("genomes: " + genomes);

        /*
         private final WeakReference<List<E>> listRef;

         public ListContentBinding(List<E> list) {
         this.listRef = new WeakReference<List<E>>(list);
         }

         @Override
         public void onChanged(Change<? extends E> change) {
         final List<E> list = listRef.get();
         if (list == null) {
         change.getList().removeListener(this);
         } else {
         while (change.next()) {
         if (change.wasPermutated()) {
         list.subList(change.getFrom(), change.getTo()).clear();
         list.addAll(change.getFrom(), change.getList().subList(change.getFrom(), change.getTo()));
         } else {
         if (change.wasRemoved()) {
         list.subList(change.getFrom(), change.getFrom() + change.getRemovedSize()).clear();
         }
         if (change.wasAdded()) {
         list.addAll(change.getFrom(), change.getAddedSubList());
         }
         }
         }
         }
         }
         */
        genomeSplitPaneNodes = EasyBind.map(model.getGenomes(),
                (Genome g) -> {
                    String text = String.format("%s:%s", g.getId(), g.getDescription());
                    System.out.println(text);
                    return new BorderPane(new Label(text));
//                        return text;
                });

        Function<Genome, Node> f = (Genome g) -> {
            String text = String.format("%s:%s", g.getId(), g.getDescription());
            System.out.println(text);
            return new BorderPane(new Label(text));
        };

        Function<String, Node> f2 = (String text) -> {
            return new BorderPane(new Label(text));
        };

        Function<Genome, String> f3 = (Genome g) -> {
            String text = String.format("%s:%s", g.getId(), g.getDescription());
            System.out.println(text);
            return text;
        };

        genomeSplitPaneNodes = FXCollections.observableArrayList();

        // listen for changes in the set of genomes and update the list of nodes to be 
        // displayed in the split pane
        genomes.addListener(new ListChangeListener<Genome>() {

            class TrackCell<T> extends ListCell<Track<T>> {

                protected void updateItem(Track<T> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        // set the graphic for the track
                        setGraphic(item.getContent());

                        // add the context menu so the track can be configured 
                        // and the view strategy changed
                        MenuItem[] items = item.getViews().stream().map(
                                (View<T> v) -> {
                                    MenuItem mi = new MenuItem(v.toString());
                                    mi.setOnAction(
                                            (ActionEvent event) -> {
                                                item.setView(v);
                                            }
                                    );
                                    return mi;
                                }
                        ).collect(Collectors.toList()).toArray(new MenuItem[0]);
                        ContextMenu menu = new ContextMenu(items);
                        setContextMenu(menu);

                    }
                }
            }

            public void onChanged(ListChangeListener.Change<? extends Genome> c) {
//                System.out.println(c.getAddedSubList().get(0).getId());
                System.out.println("changed genomes");
                genomeSplitPaneNodes.clear();

                Function<Genome, ListView<Track>> createListView = (Genome g) -> {
                    ListView<Track> trackListView = new ListView<>();

//                    model.getTracks(g).addListener(new ListChangeListener<Track>() {
//                        public void onChanged(ListChangeListener.Change<? extends Track> c) {
////                            List<String> map = model.getTracks(g).stream().map(Track::getName).collect(Collectors.toList());
//                            List<Track> map = model.getTracks(g);
//                            
//                            trackListView.getItems().setAll(map);
//                        }
//                    });
                    ObservableList<Track> tracks = model.getTracks(g);
                    Callback<Track, Observable[]> extractor = new Callback<Track, Observable[]>() {

                        @Override
                        public Observable[] call(Track param) {
                            return new Observable[]{param.getSpan(), param.getView()};
                        }
                    };

                    ObservableList<Track> observableTrackList = FXCollections.observableArrayList(extractor);
                    Bindings.bindContent(observableTrackList, tracks);
                    observableTrackList.addListener(new ListChangeListener<Track>() {

                        @Override
                        public void onChanged(ListChangeListener.Change<? extends Track> c) {
                            while (c.next()) {
                                if (c.wasUpdated()) {
                                    System.out.println("list update (span) detected");
                                }
                            }
                        }
                    });

                    trackListView.setItems(observableTrackList);

                    trackListView.setCellFactory((ListView<Track> view) -> {
                        TrackCell cell = new TrackCell();
                        cell.setPrefHeight(100);
                        return cell;
                    });

                    return trackListView;
                };

                if (genomes.size() > 0) {
                    Genome g = genomes.get(0);
                    ListView<Track> apply = createListView.apply(g);
                    genomeSplitPaneNodes.add(apply);
                }
                for (int i = 1; i < genomes.size(); i++) {
                    // add a track to represent the alignment between sequential genomes
                    genomeSplitPaneNodes.add(f2.apply(f3.apply(genomes.get(i - 1)) + " aligned to " + f3.apply(genomes.get(i))));
//                    genomeSplitPaneNodes.add(f.apply(genomes.get(i)));
                    Genome g = genomes.get(i);
                    ListView<Track> apply = createListView.apply(g);
                    genomeSplitPaneNodes.add(apply);
                }
            }
        });

        genomeSplitPaneNodes.addListener(new ListChangeListener<Node>() {

            public void onChanged(ListChangeListener.Change<? extends Node> c) {
//                System.out.println(c.getAddedSubList().get(0).getId());
                System.out.println("changed map");
            }
        });

        sp.getStyleClass().add("genome-split-pane");
//        sp.getItems().addListener((ListChangeListener.Change<? extends Node> e)->{System.out.println("changed view");});
        Bindings.bindContentBidirectional(sp.getItems(), genomeSplitPaneNodes);

//        ObservableList<Integer> oal = FXCollections.observableArrayList(1,2,3);
//        
//        class abc {
//            int i;
//
//            public abc(int i) {
//                this.i = i;
//            }
//
//            @Override
//            public String toString() {
//                return i+"";
//            }
//            
//            
//        }
//        ObservableList<abc> mapped_oal = EasyBind.map(oal, 
//                (Integer i) -> { return new abc(i); }
//        );
//        mapped_oal.addListener(new ListChangeListener<abc>() {
//
//            @Override
//            public void onChanged(ListChangeListener.Change<? extends abc> c) {
//                System.out.println("changed mapped");
//            }
//        });
//        
//        List<abc> l = new ArrayList<>();
//        Bindings.bindContent(l, mapped_oal);
//        System.out.println(mapped_oal);
////        mapped_oal.remove("2");
//        oal.remove(new Integer(2));
//        oal.add(4);
//        System.out.println(mapped_oal);
//        System.out.println(l);
//        System.exit(0);
        Rectangle r = new Rectangle(10, 20);

        Rectangle r2 = new Rectangle(30, 40, 10, 20);

        r.xProperty().bind(x);
        r2.xProperty().bind(x.add(10));

        canvas.getChildren().addAll(r, r2);

        Rectangle r3 = new Rectangle(10, 20);
        Rectangle r4 = new Rectangle(30, 40, 10, 20);
        r3.xProperty().bind(x);
        r4.xProperty().bind(x.add(10));

        Label lbl = new Label("hi");

        sim = new SimpleTrack();
//        ModelSingleton.getInstance().genomeSpanProperty().addListener(sim);
        stp.setCenter(sim);

//        lv1.widthProperty().addListener(
//                new ChangeListener<Number>() {
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                System.err.println("listening1");
//                double[] d = sp.getDividerPositions();
//                sp.setDividerPosition(1, 1-(d[0]/2));
//            }
//        }
//        );
//        lv2.widthProperty().addListener(
//                new ChangeListener<Number>() {
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                System.err.println("listening2");
//            }
//        }
//        );
//        lv3.widthProperty().addListener(
//                new ChangeListener<Number>() {
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                System.err.println("listening3");
//            }
//        }
//                
//        );
    }
}
