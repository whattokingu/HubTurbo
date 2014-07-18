package ui;

import java.io.File;
import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.control.NotificationPane;

import service.ServiceManager;

public class UI extends Application {

	// Main UI elements
	
	private Stage mainStage;

	private ColumnControl columns;
	private NotificationPane notificationPane;

	private SidePanel sidePanel;
	private MenuControl menuBar;
	
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {

		mainStage = stage;
		stage.setMaximized(true);
		Scene scene = new Scene(createRoot(), 800, 600);
		setupMainStage(scene);
		applyCSS(scene);
		
		getUserCredentials();
	}
	
	private void getUserCredentials() {
		new LoginDialog(mainStage, columns).show().thenApply(success -> {
			if (!success) {
//				getUserCredentials();
				mainStage.close();
			} else {
				columns.loadIssues();
				sidePanel.refresh();
				mainStage.setTitle("HubTurbo (" + ServiceManager.getInstance().getRemainingRequests() + " requests remaining out of " + ServiceManager.getInstance().getRequestLimit() + ")");
			}
			return true;
		}).exceptionally(e -> {
			e.printStackTrace();
			return false;
		});
	}

	private static final String CSS = "file:///" + new File("hubturbo.css").getAbsolutePath().replace("\\", "/");

	public static void applyCSS(Scene scene) {
		scene.getStylesheets().clear();
		scene.getStylesheets().add(CSS);
	}

	private void setupMainStage(Scene scene) {
		mainStage.setTitle("HubTurbo");
		mainStage.setMinWidth(800);
		mainStage.setMinHeight(600);
		mainStage.setScene(scene);
		mainStage.show();
		mainStage.setOnCloseRequest(e -> {
			ServiceManager.getInstance().stopModelUpdate();
			columns.saveSession();
		});
		
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN), columns::createNewSearchPanelAtStart);
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN), columns::createNewSearchPanelAtEnd);
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN), sidePanel::onCreateIssueHotkey);
	}

	private Parent createRoot() throws IOException {

		notificationPane = new NotificationPane();
		sidePanel = new SidePanel(mainStage, ServiceManager.getInstance().getModel());
		columns = new ColumnControl(mainStage, ServiceManager.getInstance().getModel(), notificationPane, sidePanel);
		sidePanel.setColumns(columns);
		notificationPane.setContent(columns);
		menuBar = new MenuControl(mainStage, ServiceManager.getInstance().getModel(), columns, this);
		
		ScrollPane columnsScroll = new ScrollPane(columns);
		columnsScroll.setFitToHeight(true);
		columnsScroll.setVbarPolicy(ScrollBarPolicy.NEVER);
		HBox.setHgrow(columnsScroll, Priority.ALWAYS);
		
		HBox centerContainer = new HBox();
		centerContainer.getChildren().addAll(sidePanel, columnsScroll);

        BorderPane root = new BorderPane();
		root.setTop(menuBar);
		root.setCenter(centerContainer);
		root.setRight(new GlobalButtonPanel(columns));

//		Parent panel = FXMLLoader.load(getClass().getResource("/SidePanelTabs.fxml"));
//		((TabPane) panel).getTabs().get(0).setContent(new ManageLabelsDialog(mainStage, ServiceManager.getInstance().getModel()).initialise());
//        sideContainer.setDividerPositions(0.4);

		return root;
	}
}
