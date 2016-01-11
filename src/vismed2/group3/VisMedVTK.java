package vismed2.group3;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import vismed2.group3.dicom.DicomExporter;
import vismed2.group3.filters.GradientFilter;
import vismed2.group3.filters.MIP;
import vismed2.group3.filters.MedianFilter;
import vismed2.group3.filters.ThresholdFilter;
import vismed2.group3.filters.VtkJavaFilter;
import vtk.vtkDICOMImageReader;
import vtk.vtkImageData;
import vtk.vtkNativeLibrary;

public class VisMedVTK extends JPanel implements ChangeListener, ActionListener {
	private static final long serialVersionUID = 1L;
	private vtkDICOMImageReader dicomReader;
	private vtkImageData currentImageData;
	private vtkImageData currentImageData_backup;
	private ImageViewerPanel panel0;
	private ImageViewerPanel panel1;
	private ImageViewerPanel panel2;
	private JSlider sliceSlider0;
	private JSlider sliceSlider1;
	private JSlider sliceSlider2;
	private JLabel sliceSliderLabel0;
	private JLabel sliceSliderLabel1;
	private JLabel sliceSliderLabel2;
	private int currentSlice0 = 0;
	private int currentSlice1 = 0;
	private int currentSlice2 = 0;
	private JButton buttonApplyFilter;
	private JButton buttonExport;
	private static StatusBar statusBar;
	private ProgressMonitor progressMonitor;
	private boolean crosshairsFlag = false;
	private JComboBox comboBoxFilterSelector;
	private JComboBox comboBoxSliceSelector;
	String[] filterSelectorItemsAllSlices = { "Median", "Treshold" };
	String[] filterSelectorItemsActiveSlice = { "Gradient XY", "Median", "MIP", "Roberts", "Sobel", "Treshold" };
	String[] filterSliceSelectorItems = { "Active slice", "All slices" };

	public VisMedVTK() {
		super(new BorderLayout());

		// Get DICOM image data
		dicomReader = new vtkDICOMImageReader();
		File directory = new File("data/Bassin");
		dicomReader.SetDirectoryName(directory.getAbsolutePath());
		dicomReader.Update();
		currentImageData = dicomReader.GetOutput();

		currentImageData_backup = new vtkImageData();
		currentImageData_backup.Initialize();
		currentImageData_backup.CopyStructure(currentImageData);
		currentImageData_backup.CopyAttributes(currentImageData);
		currentImageData_backup.DeepCopy(currentImageData);

		panel0 = new ImageViewerPanel(currentImageData);
		panel1 = new ImageViewerPanel(currentImageData);
		panel2 = new ImageViewerPanel(currentImageData);

		JPanel content = new JPanel(new MigLayout("fill, gap 1, insets 0, wrap 2, debug"));

		// Prepare controls
		JPanel controlsPanel = new JPanel(new MigLayout());

		JPanel sliderPanel = new JPanel(new MigLayout("wrap 2, fillx"));
		sliderPanel.setBorder(BorderFactory.createTitledBorder("Slices"));
		sliceSlider0 = new JSlider(JSlider.HORIZONTAL, panel0.getSliceMin(), panel0.getSliceMax(), currentSlice0);
		sliceSlider0.addChangeListener(this);
		sliceSliderLabel0 = new JLabel(String.format("%d/%d", sliceSlider0.getValue(), sliceSlider0.getMaximum()));
		sliceSlider1 = new JSlider(JSlider.HORIZONTAL, panel1.getSliceMin(), panel1.getSliceMax(), currentSlice1);
		sliceSlider1.addChangeListener(this);
		sliceSliderLabel1 = new JLabel(String.format("%d/%d", sliceSlider1.getValue(), sliceSlider1.getMaximum()));
		sliceSlider2 = new JSlider(JSlider.HORIZONTAL, panel2.getSliceMin(), panel2.getSliceMax(), currentSlice2);
		sliceSlider2.addChangeListener(this);
		sliceSliderLabel2 = new JLabel(String.format("%d/%d", sliceSlider2.getValue(), sliceSlider2.getMaximum()));
		sliderPanel.add(sliceSlider0);
		sliderPanel.add(sliceSliderLabel0);
		sliderPanel.add(sliceSlider1);
		sliderPanel.add(sliceSliderLabel1);
		sliderPanel.add(sliceSlider2);
		sliderPanel.add(sliceSliderLabel2);

		JPanel filterPanel = new JPanel(new MigLayout());
		filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));

		filterPanel.add(new JLabel("Apply filter on:"));
		comboBoxSliceSelector = new JComboBox(filterSliceSelectorItems);
		comboBoxSliceSelector.addActionListener(this);
		filterPanel.add(comboBoxSliceSelector, "wrap");

		filterPanel.add(new JLabel("Filter:"));
		comboBoxFilterSelector = new JComboBox(filterSelectorItemsActiveSlice);
		comboBoxFilterSelector.addActionListener(this);
		filterPanel.add(comboBoxFilterSelector, "");
		buttonApplyFilter = new JButton("Apply");
		buttonApplyFilter.addActionListener(this);
		filterPanel.add(buttonApplyFilter, "wrap");

		buttonExport = new JButton("Export as DICOM");
		buttonExport.addActionListener(this);
		filterPanel.add(buttonExport, "");

		controlsPanel.add(sliderPanel, "wrap");
		controlsPanel.add(filterPanel);

		content.add(panel0, "grow");
		content.add(panel1, "grow");
		content.add(panel2, "grow");
		content.add(controlsPanel, "");

		add(content, BorderLayout.CENTER);
		statusBar = new StatusBar();
		add(statusBar, BorderLayout.SOUTH);

		Runnable initViewsDelayRunnable = new Runnable() {
			public void run() {
				try {
					Thread.sleep(1500);
					panel0.getImageViewer().GetVtkImageViewer().SetSliceOrientationToXY();
					panel1.getImageViewer().GetVtkImageViewer().SetSliceOrientationToXZ();
					panel2.getImageViewer().GetVtkImageViewer().SetSliceOrientationToYZ();
					panel0.getImageViewer().GetVtkImageViewer().Render();
					panel1.getImageViewer().GetVtkImageViewer().Render();
					panel2.getImageViewer().GetVtkImageViewer().Render();

					// Set slider values since they depend on orientation
					sliceSlider0.setMinimum(panel0.getImageViewer().GetVtkImageViewer().GetSliceMin());
					sliceSlider0.setMaximum(panel0.getImageViewer().GetVtkImageViewer().GetSliceMax());
					currentSlice0 = panel0.getImageViewer().GetVtkImageViewer().GetSlice();
					sliceSliderLabel0
							.setText(String.format("%d/%d", sliceSlider0.getValue(), sliceSlider0.getMaximum()));
					sliceSlider1.setMinimum(panel1.getImageViewer().GetVtkImageViewer().GetSliceMin());
					sliceSlider1.setMaximum(panel1.getImageViewer().GetVtkImageViewer().GetSliceMax());
					currentSlice1 = panel1.getImageViewer().GetVtkImageViewer().GetSlice();
					sliceSliderLabel1
							.setText(String.format("%d/%d", sliceSlider1.getValue(), sliceSlider1.getMaximum()));
					sliceSlider2.setMinimum(panel2.getImageViewer().GetVtkImageViewer().GetSliceMin());
					sliceSlider2.setMaximum(panel2.getImageViewer().GetVtkImageViewer().GetSliceMax());
					currentSlice2 = panel2.getImageViewer().GetVtkImageViewer().GetSlice();
					sliceSliderLabel2
							.setText(String.format("%d/%d", sliceSlider2.getValue(), sliceSlider2.getMaximum()));
				} catch (InterruptedException iex) {
				}
			}
		};
		Thread initViewsDelayThread = new Thread(initViewsDelayRunnable);
		initViewsDelayThread.start();
	}

	public static void main(String s[]) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame("VisMed2 VTK Gruppe 3");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().setLayout(new BorderLayout());
				frame.getContentPane().add(new VisMedVTK(), BorderLayout.CENTER);
				frame.setSize(700, 700);
				frame.setVisible(true);
			}
		});
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(sliceSlider0)) {
			currentSlice0 = sliceSlider0.getValue();
			if (crosshairsFlag) {
				panel0.setInputData(currentImageData_backup);
			}
			panel0.setSlice(currentSlice0);
			sliceSliderLabel0.setText(String.format("%d/%d", sliceSlider0.getValue(), sliceSlider0.getMaximum()));
		} else if (e.getSource().equals(sliceSlider1)) {
			currentSlice1 = sliceSlider1.getValue();
			if (crosshairsFlag) {
				panel1.setInputData(currentImageData_backup);
			}
			panel1.setSlice(currentSlice1);
			sliceSliderLabel1.setText(String.format("%d/%d", sliceSlider1.getValue(), sliceSlider1.getMaximum()));
		} else if (e.getSource().equals(sliceSlider2)) {
			currentSlice2 = sliceSlider2.getValue();
			if (crosshairsFlag) {
				panel2.setInputData(currentImageData_backup);
			}
			panel2.setSlice(currentSlice2);
			sliceSliderLabel2.setText(String.format("%d/%d", sliceSlider2.getValue(), sliceSlider2.getMaximum()));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(buttonApplyFilter)) {
			if (comboBoxFilterSelector.getSelectedItem().equals("Gradient XY")) {
				if (comboBoxSliceSelector.getSelectedItem().equals("Active slice")) {
					GradientFilter gradient = new GradientFilter();
					gradient.setFilter(GradientFilter.Type.GradientXY);
					crosshairsFlag = gradient.setAllSlices(false);
					gradient.setSlice(panel0.getSlice(), panel1.getSlice(), panel2.getSlice());
					applyFilter(gradient);
				}
			} else if (comboBoxFilterSelector.getSelectedItem().equals("Sobel")) {
				if (comboBoxSliceSelector.getSelectedItem().equals("Active slice")) {
					GradientFilter gradient = new GradientFilter();
					gradient.setFilter(GradientFilter.Type.Sobel);
					crosshairsFlag = gradient.setAllSlices(false);
					gradient.setSlice(panel0.getSlice(), panel1.getSlice(), panel2.getSlice());
					applyFilter(gradient);
				}
			} else if (comboBoxFilterSelector.getSelectedItem().equals("Roberts")) {
				if (comboBoxSliceSelector.getSelectedItem().equals("Active slice")) {
					GradientFilter gradient = new GradientFilter();
					gradient.setFilter(GradientFilter.Type.Roberts);
					crosshairsFlag = gradient.setAllSlices(false);
					gradient.setSlice(panel0.getSlice(), panel1.getSlice(), panel2.getSlice());
					applyFilter(gradient);
				}
			} else if (comboBoxFilterSelector.getSelectedItem().equals("Median")) {
				MedianFilter median = new MedianFilter();
				median.SetKernelSize(3, 3, 3);
				if (comboBoxSliceSelector.getSelectedItem().equals("All slices")) {
					crosshairsFlag = median.setAllSlices(true);
				} else {
					crosshairsFlag = median.setAllSlices(false);
				}
				median.setSlice(panel0.getSlice(), panel1.getSlice(), panel2.getSlice());
				applyFilter(median);
			} else if (comboBoxFilterSelector.getSelectedItem().equals("Treshold")) {
				ThresholdFilter threshold = new ThresholdFilter();
				if (comboBoxSliceSelector.getSelectedItem().equals("All slices")) {
					crosshairsFlag = threshold.setAllSlices(true);
				} else {
					crosshairsFlag = threshold.setAllSlices(false);
				}
				threshold.setUpperThreshold(1000.0);
				threshold.setLowerThreshold(500.0);
				threshold.setSlice(panel0.getSlice(), panel1.getSlice(), panel2.getSlice());
				applyFilter(threshold);
			} else if (comboBoxFilterSelector.getSelectedItem().equals("MIP")) {
				if (comboBoxSliceSelector.getSelectedItem().equals("Active slice")) {
					MIP mip = new MIP();
					crosshairsFlag = true;
					mip.setSlice(panel0.getSlice(), panel1.getSlice(), panel2.getSlice());
					applyFilter(mip);
				}
			}
		} else if (e.getSource().equals(buttonExport)) {
			exportCurrentImage();
		} else if (e.getSource().equals(comboBoxSliceSelector)) {
			if (comboBoxSliceSelector.getSelectedItem().equals("All slices")) {
				comboBoxFilterSelector.removeAllItems();
				for (int i = 0; i < filterSelectorItemsAllSlices.length; i++) {
					comboBoxFilterSelector.addItem(filterSelectorItemsAllSlices[i]);
				}
			} else {
				comboBoxFilterSelector.removeAllItems();
				for (int i = 0; i < filterSelectorItemsActiveSlice.length; i++) {
					comboBoxFilterSelector.addItem(filterSelectorItemsActiveSlice[i]);
				}
			}
		}
	}

	private void applyFilter(final VtkJavaFilter filter) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		statusBar.setMessage("Applying filter " + filter.getFilterName() + "...");
		SwingWorker<VtkJavaFilter, Void> worker = new SwingWorker<VtkJavaFilter, Void>() {

			private VtkJavaFilter algorithm;

			@Override
			public VtkJavaFilter doInBackground() {
				algorithm = filter;
				algorithm.applyFilter(currentImageData);
				return algorithm;
			}

			@Override
			public void done() {
				setCursor(Cursor.getDefaultCursor());
				statusBar.setMessage("Ready");
				try {
					currentImageData = get().GetOutput();
					panel0.setInputData(currentImageData);
					panel1.setInputData(currentImageData);
					panel2.setInputData(currentImageData);
					panel0.getImageViewer().Render();
					panel1.getImageViewer().Render();
					panel2.getImageViewer().Render();
				} catch (InterruptedException ignore) {
				} catch (java.util.concurrent.ExecutionException e) {
					String why = null;
					Throwable cause = e.getCause();
					if (cause != null) {
						why = cause.getMessage();
					} else {
						why = e.getMessage();
					}
					System.err.println("Error applying filter: " + why);
				}
			}
		};
		worker.execute();
	}

	private void exportCurrentImage() {
		final String msgTemplate = "Completed %d of %d slices.\n";
		final int numberOfImagesToExport = currentImageData.GetDimensions()[2];

		class ExportTask extends SwingWorker<Void, Void>implements ChangeListener {
			@Override
			public Void doInBackground() {
				DicomExporter exporter = new DicomExporter();
				exporter.setChangeListener(this);
				exporter.exportImageData(currentImageData, "data/output/test");
				return null;
			}

			@Override
			public void done() {
				buttonExport.setEnabled(true);
			}

			@Override
			public void stateChanged(ChangeEvent e) {
				int progress = (Integer) e.getSource();
				progressMonitor.setProgress(progress);
				String message = String.format(msgTemplate, progress, numberOfImagesToExport);
				progressMonitor.setNote(message);
				if (progressMonitor.isCanceled() || isDone()) {
					if (progressMonitor.isCanceled()) {
						cancel(true);
						System.out.println("Task canceled.\n");
					} else {
						System.out.println("Task completed.\n");
					}
					buttonExport.setEnabled(true);
				}
			}
		}

		progressMonitor = new ProgressMonitor(this, "Exporting current image to DICOM",
				String.format(msgTemplate, 0, numberOfImagesToExport), 0, numberOfImagesToExport);
		progressMonitor.setProgress(0);
		buttonExport.setEnabled(false);
		ExportTask task = new ExportTask();
		task.execute();
	}

	public static void setStatusBar(final String status) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				statusBar.setMessage(status);
			}
		});
	}

	// Load VTK library and print which library was not properly loaded
	static {
		if (!vtkNativeLibrary.LoadAllNativeLibraries()) {
			for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
				if (!lib.IsLoaded()) {
					System.out.println(lib.GetLibraryName() + " not loaded");
				}
			}
		}
		vtkNativeLibrary.DisableOutputWindow(null);
	}
}
