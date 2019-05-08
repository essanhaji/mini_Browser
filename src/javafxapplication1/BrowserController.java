package JavaFXApplication1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker.State;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebHistory.Entry;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import outils.HistoryObject;
import outils.SearchEngine;

public class BrowserController implements Initializable {

    @FXML
    private TabPane tabPane;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    MenuItem homePageBackgroundImg;

    @FXML
    Label downloadStatusLabel;

    @FXML
    private Menu historyMenu = new Menu();

    @FXML
    private AnchorPane downloadAnchorPane;

    //handle download tasks
    private class DownloadTask extends Task<Void> {

        private String url;

        public DownloadTask(String url) {
            this.url = url;
        }

        @Override
        protected Void call() {
            try {
                String ext = url.substring(url.lastIndexOf("."), url.length());
                URLConnection connection = new URL(url).openConnection();
                long fileLength = connection.getContentLengthLong();

                try (InputStream is = connection.getInputStream();
                        OutputStream os = Files.newOutputStream(Paths.get("downloadedfile" + ext))) {
                    long nread = 0L;
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = is.read(buf)) > 0) {
                        os.write(buf, 0, n);
                        nread += n;
                        updateProgress(nread, fileLength);
                    }
                }
                return null;
            } catch (MalformedURLException ex) {
            } catch (IOException ex) {
            }
            return null;
        }

        @Override
        protected void failed() {
            System.out.println("failed");
            downloadStatusLabel.setText("Download failed!");
        }

        @Override
        protected void succeeded() {
            System.out.println("downloaded");
            downloadStatusLabel.setText("File download complete");
        }
    }

    private Parent createContent() {
        VBox root = new VBox();
        root.setPrefSize(300, 400);
        TextField fieldURL = new TextField();
        fieldURL.setPromptText("⚭ enter download link here");
        root.getChildren().addAll(fieldURL);
        fieldURL.setOnAction(event -> {
            downloadStatusLabel.setText("Downloading...");
            Task<Void> task = new DownloadTask(fieldURL.getText());
            ProgressBar progressBar = new ProgressBar();
            progressBar.setPrefWidth(350);
            progressBar.progressProperty().bind(task.progressProperty());
            root.getChildren().add(progressBar);
            fieldURL.clear();
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        });
        return root;
    }

    SearchEngine srcEng = new SearchEngine("google", "http://www.google.com");

    @FXML
    private CheckMenuItem googleMenuItm;
    @FXML
    private CheckMenuItem bingMenuItm;

    ObservableList<String> histItems = FXCollections.observableArrayList();

    int EtG = 1, EtB = 0;

    @FXML
    private void setEngine() {
        if (googleMenuItm.isSelected() && bingMenuItm.isSelected()) {
            if (EtG > EtB) {
                googleMenuItm.setSelected(false);
                EtG = 0;
                EtB = 1;
                srcEng.setEngine("bing");
                System.out.println("Bing is the eninge and Google is disabled.");
            } else {
                bingMenuItm.setSelected(false);
                EtG = 1;
                EtB = 0;
                srcEng.setEngine("google");
                System.out.println("Google is the eninge and Bing is disabled.");
            }
        } else if (googleMenuItm.isSelected()) {
            System.out.println("Inside google");
            srcEng.setEngine("google");
            System.out.println("Google is the eninge and Bing is disabled.");
            EtG = 1;
        } else if (bingMenuItm.isSelected()) {
            System.out.println("Inside Bing.");
            srcEng.setEngine("bing");
            System.out.println("Bing is the eninge and Google is disabled.");
            EtB = 1;
        }

    }

    public class NewTab {

        //properties
        private final Tab newTab;
        private final AnchorPane smallAnchor;
        private final ToolBar toolBar;
        private final Label label;
        private final MenuBar menuBar;
        private final Menu bookmarksMenu, settingsMenu, helpMenu;
        private final HBox hBox;
        private final TextField urlBox;
        private final Button goButton;
        private final Button backButton;
        private final Button forwardButton;
        private final Button reloadButton;
        private final BorderPane borderPane;
        private MyBrowser myBrowser;

        //methods
        public NewTab() {
            newTab = new Tab();
            smallAnchor = new AnchorPane();
            toolBar = new ToolBar();
            label = new Label();
            menuBar = new MenuBar();
            bookmarksMenu = new Menu();
            settingsMenu = new Menu();
            helpMenu = new Menu();
            hBox = new HBox();
            urlBox = new TextField();
            goButton = new Button();
            backButton = new Button();
            forwardButton = new Button();
            reloadButton = new Button();
            borderPane = new BorderPane();
        }

        public Tab createTab() {
            goButton.setText("Go");
            newTab.setText("New Tab");

            backButton.setText("◁");
            forwardButton.setText("▷");
            toolBar.getItems().addAll(backButton, forwardButton);
            toolBar.setPrefHeight(40);
            toolBar.setPrefWidth(549);
            AnchorPane.setTopAnchor(toolBar, 0.0);
            AnchorPane.setLeftAnchor(toolBar, 0.0);
            AnchorPane.setRightAnchor(toolBar, 0.0);
            smallAnchor.getChildren().add(toolBar);

            bookmarksMenu.setText("Bookmarks");
            settingsMenu.setText("Settings");
            helpMenu.setText("Help");

            menuBar.getMenus().addAll(bookmarksMenu, settingsMenu, helpMenu);
            menuBar.setPrefWidth(190);
            menuBar.setPrefHeight(40);
            menuBar.setPadding(new Insets(6, 0, 0, 0));
            AnchorPane.setRightAnchor(menuBar, 0.0);

            urlBox.setPromptText("🔎 enter your url here or search something");
            urlBox.setPrefHeight(30);
            urlBox.setPrefWidth(391);
            goButton.setPrefHeight(30);
            goButton.setPrefWidth(32);
            reloadButton.setText("↺");
            reloadButton.setPrefHeight(30);
            reloadButton.setPrefWidth(24);

            hBox.getChildren().addAll(urlBox, goButton, reloadButton);
            hBox.setSpacing(5.0);
            AnchorPane.setTopAnchor(hBox, 5.0);
            AnchorPane.setLeftAnchor(hBox, 60.0);
            smallAnchor.getChildren().add(hBox);

            AnchorPane.setTopAnchor(label, 10.0);
            AnchorPane.setLeftAnchor(label, 520.0);
            smallAnchor.getChildren().add(label);

            AnchorPane.setTopAnchor(borderPane, 40.0);
            AnchorPane.setBottomAnchor(borderPane, 0.0);
            AnchorPane.setLeftAnchor(borderPane, 0.0);
            AnchorPane.setRightAnchor(borderPane, 0.0);
            smallAnchor.getChildren().add(borderPane);

            newTab.setContent(smallAnchor);
            newTab.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event arg) {
                    try {
                        System.out.println("A tab closed.");
                        newTabBtnPosLeft();
                        myBrowser.closeWindow();
                    } catch (Exception e) {
                    }
                }
            });

            backButton.setOnMouseClicked((MouseEvent me) -> {
                System.out.println("Back button has been pressed.");
                myBrowser.goBack();
                label.setText("");
            });

            forwardButton.setOnMouseClicked((MouseEvent me) -> {
                System.out.println("Forward button has been pressed.");
                myBrowser.goForward();
                label.setText("");
            });

            AnchorPane.setTopAnchor(tabPane, 0.0);
            AnchorPane.setBottomAnchor(tabPane, 0.0);
            AnchorPane.setLeftAnchor(tabPane, 0.0);
            AnchorPane.setRightAnchor(tabPane, 0.0);

            goButton.setOnAction((ActionEvent e) -> {
                goButtonPressed();
            });

            reloadButton.setOnAction((ActionEvent e) -> {
                myBrowser.reloadWebPage();
            });

            urlBox.setOnAction((ActionEvent e) -> {
                goButtonPressed();
            });

            return newTab;
        }

        public void goToURL(String urlStr) {
            myBrowser = new MyBrowser(urlStr);
            borderPane.setCenter(myBrowser);
        }

        public void goButtonPressed() {
            label.setText("");
            String urlStr;
            if (urlBox.getText() != null && !urlBox.getText().isEmpty()) {
                if (!urlBox.getText().contains(".")) {
                    srcEng.setUrlStr(urlBox.getText());
                    urlStr = srcEng.getEngineSpecificUrl();
                } else if (!urlBox.getText().startsWith("http://")) {
                    urlStr = "http://" + urlBox.getText();
                } else if (!urlBox.getText().startsWith("http://www.")) {
                    urlStr = "http://www." + urlBox.getText();
                } else {
                    urlStr = urlBox.getText();
                }

                myBrowser = new MyBrowser(urlStr);
                borderPane.setCenter(myBrowser);
            } else {
                label.setText("You didn't enter anything (0_0) ");
            }
        }

        public void setTabBackground(String imageFileLocation) {
            ImageView iv = new ImageView();
            Image img = new Image(imageFileLocation);
            iv.setImage(img);
            borderPane.setCenter(iv);
        }

        public void setTabContent(MyBrowser passedBroser) {
            borderPane.setClip(passedBroser);
        }

        public class MyBrowser extends Region {

            WebView browser = new WebView();
            final WebEngine webEngine = browser.getEngine();
            WebHistory history = webEngine.getHistory();

            public MyBrowser(String url) {
                try {
                    webEngine.getLoadWorker().stateProperty().addListener(
                            new ChangeListener<State>() {
                        @Override
                        public void changed(ObservableValue ov, State oldState, State newState) {
                            ProgressIndicator progInd = new ProgressIndicator(-1.0);
                            progInd.setPrefHeight(17);
                            progInd.setPrefWidth(25);
                            newTab.setGraphic(progInd);

                            reloadButton.setText("X");
                            reloadButton.setOnAction((ActionEvent e) -> {
                                myBrowser.closeWindow();
                                newTab.setText("Aborted!");
                                label.setText("You have aborted loading the page.");
                                newTab.setGraphic(null);
                            });

                            if (newState == State.SUCCEEDED) {
                                label.setText("");
                                newTab.setText(webEngine.getTitle());
                                urlBox.setText(webEngine.getLocation());
                                newTab.setGraphic(loadFavicon(url));
                                reloadButton.setText(("↺"));
                                reloadButton.setOnAction((ActionEvent e) -> {
                                    myBrowser.reloadWebPage();
                                });

                                EventListener listener = new EventListener() {
                                    public void handleEvent(Event ev) {
                                        System.out.println("You pressed on a link");
                                    }
                                };

                                Document doc = webEngine.getDocument();
                                NodeList el = doc.getElementsByTagName("a");
                                for (int i = 0; i < el.getLength(); i++) {
                                }
                            }
                        }
                    });
                    try {

                    history.getEntries().addListener((ListChangeListener.Change<? extends Entry> c) -> {
                        c.next();
                        c.getRemoved().forEach(new Consumer<Entry>() {
                            @Override
                            public void accept(Entry e) {
                                boolean remove = historyMenu.getItems().remove(e.getUrl());
                            }
                        });
                        c.getAddedSubList().stream().map(new Function<Entry, MenuItem>() {
                            @Override
                            public MenuItem apply(Entry e) {
                                MenuItem menuItem;
                                menuItem = new MenuItem(e.getUrl().replace(e.getUrl().substring(24), ""));
                                histObj.addHist(e.getUrl(), "hist.txt");
                                histItems.add(e.getUrl());
                                menuItem.setGraphic(loadFavicon(e.getUrl()));
                                menuItem.setOnAction((ActionEvent ev) -> {
                                    NewTab aTab1 = new NewTab();
                                    aTab1.goToURL(e.getUrl());
                                    Tab tab = aTab1.createTab();
                                    tabPane.getTabs().add(tab);
                                    tabPane.getSelectionModel().select(tab);
                                    newTabBtnPosRight();
                                });
                                return menuItem;
                            }
                        }).map((menuItem) -> {
                            historyMenu.setText(LocalDate.now().toString());
                            return menuItem;
                        }).forEachOrdered((menuItem) -> {
                            historyMenu.getItems().add(menuItem);
                        });
                    });
                    } catch (Exception e) {
                    }
                    webEngine.setCreatePopupHandler((PopupFeatures config) -> {
                        browser.setFontScale(0.8);
                        if (!getChildren().contains(browser)) {
                            getChildren().add(browser);
                        }
                        return browser.getEngine();
                    });

                    final WebView smallView = new WebView();

                    webEngine.load(url);
                    getChildren().add(browser);

                } catch (Exception e) {
                }
            }

            private void createContextMenu(WebView webView) {
                ContextMenu contextMenu = new ContextMenu();
                MenuItem reload = new MenuItem("Reload");
                reload.setOnAction(e -> webView.getEngine().reload());
                MenuItem savePage = new MenuItem("Save Page");
                savePage.setOnAction(e -> System.out.println("Save page..."));
                MenuItem openInNewWindow = new MenuItem("Open in New Window");
                openInNewWindow.setOnAction(e -> System.out.println("Open in New Window"));
                MenuItem openInNewTab = new MenuItem("Open in New Tab");
                openInNewTab.setOnAction(e -> System.out.println("Open in New Tab"));
                contextMenu.getItems().addAll(reload, savePage, openInNewWindow, openInNewTab);

                webView.setOnMousePressed(e -> {
                    if (e.getButton() == MouseButton.SECONDARY) {
                        contextMenu.show(webView, e.getScreenX(), e.getScreenY());
                    } else {
                        contextMenu.hide();
                    }
                });
            }

            public void goBack() {
                final WebHistory history = webEngine.getHistory();
                ObservableList<WebHistory.Entry> entryList = history.getEntries();
                int currentIndex = history.getCurrentIndex();

                Platform.runLater(()
                        -> {
                    history.go(entryList.size() > 1
                            && currentIndex > 0
                                    ? -1
                                    : 0);
                });
            }

            public void goForward() {
                try {
                    final WebHistory history = webEngine.getHistory();
                    ObservableList<WebHistory.Entry> entryList = history.getEntries();
                    int currentIndex = history.getCurrentIndex();
                    Platform.runLater(new Thread() {
                        @Override
                        public void run() {
                            try {
                                 history.go(entryList.size() > 1
                                    && currentIndex < entryList.size() - 1
                                    ? 1
                                    : 0);
                            } catch (IndexOutOfBoundsException e) {
                            }
                           
                        }
                    });
                } catch (Exception e) {
                }
            }

            public ImageView loadFavicon(String location) {
                    String faviconUrl = null;
                    if (webEngine.getTitle().equalsIgnoreCase("Google")) {
                        faviconUrl = "https://www.google.com/s2/favicons?domain_url=www.gmail.com";
                    } else {
                        try {
                            faviconUrl = String.format("http://www.google.com/s2/favicons?domain_url=%s", URLEncoder.encode(location, "UTF-8"));
                        } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(BrowserController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    Image favicon = new Image(faviconUrl, true);
                    if (favicon.equals(new Image("http://www.google.com/s2/favicons?domain_url=abc"))) {
                        ImageView iv_default = new ImageView(new Image("file:Resources/home.png"));
                        return iv_default;
                    } else {
                        ImageView iv = new ImageView(favicon);
                        return iv;
                    }
            }

            public void closeWindow() {
                browser.getEngine().load(null);
                browser = null;
            }

            public void reloadWebPage() {
                webEngine.reload();
            }

            @Override
            protected void layoutChildren() {
                double w = getWidth();
                double h = getHeight();
                layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
            }

            @Override
            protected double computePrefWidth(double height) {
                return 750;
            }

            @Override
            protected double computePrefHeight(double width) {
                return 500;
            }
        }
    }

    private double newTabLeftPadding = 102.0;

    @FXML
    private Button newTabBtn;

    @FXML
    private void newTabFunction(ActionEvent event) {
        NewTab aTab = new NewTab();
        Tab tab = aTab.createTab();
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        newTabBtnPosRight();
    }

    private void newTabBtnPosRight() {
        newTabLeftPadding += 91;
        AnchorPane.setLeftAnchor(newTabBtn, newTabLeftPadding++);
    }

    private void newTabBtnPosLeft() {
        try {
            newTabLeftPadding -= 91;
            AnchorPane.setLeftAnchor(newTabBtn, newTabLeftPadding--);
            if (newTabLeftPadding < 102.0) {
                System.out.println("All tabs closed.");
                Platform.exit();
            }
        } catch (Exception e) {
        }
    }

    @FXML
    private Label homeLabel;

    @FXML
    private void homeBtnHover() {
        homeLabel.setText("Home");
    }

    @FXML
    private void homeBtnHoverExit() {
        homeLabel.setText("");
    }

    @FXML
    private Label downloadLabel;

    @FXML
    private void downloadBtnHover() {
        downloadLabel.setText("Downloads");
    }

    @FXML
    private void downloadBtnHoverExit() {
        downloadLabel.setText("");
    }
    @FXML
    private Label bookmarkLabel;

    @FXML
    private void bookmarkBtnHover() {
        bookmarkLabel.setText("Bookmarks");
    }

    @FXML
    private void bookmarkBtnHoverExit() {
        bookmarkLabel.setText("");
    }

    @FXML
    private Button downloadButton;
    @FXML
    private Button bookmarkButton;

    private NewTab aTab = new NewTab();

    HistoryObject histObj;

    @FXML
    ListView historyList;

    @FXML
    DatePicker startDatePicker;

    @FXML
    DatePicker endDatePicker;

    @FXML
    DatePicker delStartDatePicker;

    @FXML
    DatePicker delEndDatePicker;

    @FXML
    Label delHistLabel;

    @FXML
    Button delHistButton;

    @FXML
    Label historyLabel;

    @FXML
    ListView prevHistoryListView;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        aTab.setTabBackground("file:Resources/b46c8e1cde764e377f0ed9399e6380a6.jpg");
        Tab tab = aTab.createTab();
        tab.setText("Home Tab");
        tabPane.getTabs().add(tab);

        ImageView iv = new ImageView();
        Image img = new Image("file:Resources/home.png");
        iv.setImage(img);
        iv.setFitHeight(21);
        iv.setFitWidth(20);
        homeBtn.setGraphic(iv);

        ImageView iv2 = new ImageView();
        Image img2 = new Image("file:Resources/downloadIcon.png");
        iv2.setImage(img2);
        iv2.setFitHeight(21);
        iv2.setFitWidth(20);
        downloadButton.setGraphic(iv2);

        ImageView iv3 = new ImageView();
        Image img3 = new Image("file:Resources/Bookmark-512.png");
        iv3.setImage(img3);
        iv3.setFitHeight(21);
        iv3.setFitWidth(20);
        bookmarkButton.setGraphic(iv3);

        colorPicker.setOnAction(new EventHandler() {
            public void handle(Event t) {
                System.out.println("Color choosed: " + colorPicker.getValue());
            }
        });

        histObj = new HistoryObject();
        startDatePicker.setOnAction(new EventHandler() {
            public void handle(Event t) {
                ObservableList<String> prevHistItems = FXCollections.observableArrayList();
                LocalDate date = startDatePicker.getValue();
                System.err.println("Selected date: " + date);
                ArrayList<HistoryObject> ar = new ArrayList();
                if (endDatePicker.getValue() == null) {
                    ar = histObj.getHistByDate(startDatePicker.getValue(), startDatePicker.getValue(), "hist.txt");
                    for (int i = 0; i < ar.size(); i++) {
                        prevHistItems.add(ar.get(i).url);
                    }
                    prevHistoryListView.setItems(prevHistItems);
                    prevHistoryListView.setOnMouseClicked(new EventHandler<MouseEvent>() {

                        @Override
                        public void handle(MouseEvent event) {
                            System.out.println("clicked on " + prevHistoryListView.getSelectionModel().getSelectedItem());
                            NewTab aTab = new NewTab();
                            aTab.goToURL(prevHistoryListView.getSelectionModel().getSelectedItem().toString());
                            Tab tab = aTab.createTab();
                            tabPane.getTabs().add(tab);
                            tabPane.getSelectionModel().select(tab);
                            newTabBtnPosRight();
                        }
                    });
                } else {
                    ar = histObj.getHistByDate(startDatePicker.getValue(), endDatePicker.getValue(), "hist.txt");
                    for (int i = 0; i < ar.size(); i++) {
                        prevHistItems.add(ar.get(i).url);
                    }
                    prevHistoryListView.setItems(prevHistItems);
                }
            }
        });

        endDatePicker.setOnAction(new EventHandler() {
            public void handle(Event t) {
                ObservableList<String> prevHistItems = FXCollections.observableArrayList();
                LocalDate date = endDatePicker.getValue();
                System.err.println("Selected date: " + date);
                ArrayList<HistoryObject> ar = new ArrayList();
                if (startDatePicker.getValue() == null) {
                    ar = histObj.getHistByDate(endDatePicker.getValue(), endDatePicker.getValue(), "hist.txt");
                    for (int i = 0; i < ar.size(); i++) {
                        prevHistItems.add(ar.get(i).url);
                    }
                    prevHistoryListView.setItems(prevHistItems);
                } else {
                    ar = histObj.getHistByDate(startDatePicker.getValue(), endDatePicker.getValue(), "hist.txt");
                    for (int i = 0; i < ar.size(); i++) {
                        prevHistItems.add(ar.get(i).url);
                    }
                    prevHistoryListView.setItems(prevHistItems);
                }
            }
        });

        Parent parent = createContent();
        downloadAnchorPane.getChildren().add(parent);
        downloadAnchorPane.setVisible(false);
        historyAnchorPane.setVisible(false);

    }

    @FXML
    private void backgroundImgFunction() {
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(stage);
        System.out.println("You chose this file: " + file.getAbsolutePath());
        aTab.setTabBackground("file:" + file.getAbsolutePath());
    }

    @FXML
    private Button homeBtn;

    @FXML
    private void createHomeTab() {
        System.out.println("Home button pressed.");

        aTab = new NewTab();
        aTab.setTabBackground("file:Resources/b46c8e1cde764e377f0ed9399e6380a6.jpg");
        Tab tab = aTab.createTab();
        tab.setText("Home Tab");
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        newTabBtnPosRight();
    }

    int k = 0;

    @FXML
    private void downloadButtonFunction() {
        k++;
        if (k % 2 == 0) {
            System.out.println("Download menu is now hidden.");
            downloadAnchorPane.setVisible(false);
            downloadStatusLabel.setText("");
        } else {
            downloadAnchorPane.setVisible(true);
            System.out.println("Download menu is now visible.");
            downloadAnchorPane.setStyle("-fx-background-color: gray;");
        }
    }

    int m = 0;
    @FXML
    private AnchorPane historyAnchorPane;

    @FXML
    private void historyLabelFunction() {
        delHistLabel.setText("Permanently delete history");
        m++;
        if (m % 2 == 0) {
            historyAnchorPane.setVisible(false);
        } else {
            historyAnchorPane.setVisible(true);
            System.out.println("Showing history options");
        }
    }

    @FXML
    private void bookmarkButtonFunction() {
        System.out.println("Bookmark button pressed.");
    }

    @FXML
    private void deleteHistoryFunction() {
        if (delEndDatePicker.getValue() == null) {
            delHistLabel.setText("Please enter end date.");
        }

        if (delStartDatePicker.getValue() == null) {
            delHistLabel.setText("Please enter start date.");
        }

        if (delStartDatePicker.getValue() != null && delEndDatePicker.getValue() != null) {
            delHistLabel.setText("History has been deleted.");
            histObj.deleteHistByDate(delStartDatePicker.getValue(), delEndDatePicker.getValue(), "hist.txt");
            System.out.println("Deleted history from " + delStartDatePicker.getValue() + " to " + delEndDatePicker.getValue());
        }
    }
}
