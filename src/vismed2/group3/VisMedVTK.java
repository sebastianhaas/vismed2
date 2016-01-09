package vismed2.group3;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
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
	vtkImageData currentImageData_backup;
	private ImageViewerPanel panel0;
	private ImageViewerPanel panel1;
	private ImageViewerPanel panel2;
	private JSlider sliceSlider0;
	private JSlider sliceSlider1;
	private JSlider sliceSlider2;
	private int currentSlice0 = 0;
	private int currentSlice1 = 0;
	private int currentSlice2 = 0;
	private JButton buttonFilterTreshold;
	private JButton buttonFilterMedian;
	private JButton buttonFilterGradient;
	private JButton buttonExport;
	private StatusBar statusBar;
	private ProgressMonitor progressMonitor;
	private boolean crosshairsFlag = false;

	// -----------------------------------------------------------------
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

	// -----------------------------------------------------------------
	public VisMedVTK() {
		super(new BorderLayout());

		// Get DICOM image data
		dicomReader = new vtkDICOMImageReader();
		File directory = new File("data/Dentascan-0.75-H60s-3");
		dicomReader.SetDirectoryName(directory.getAbsolutePath()); // Spaces in
																	// path
																	// causing
																	// troubles
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

		JPanel content = new JPanel(new MigLayout("fill, debug"));

		// Prepare controls
		JPanel controlsPanel = new JPanel(new MigLayout());

		JPanel sliderPanel = new JPanel(new MigLayout());
		sliderPanel.setBorder(BorderFactory.createTitledBorder("Slices"));
		sliceSlider0 = new JSlider(JSlider.HORIZONTAL, panel0.getSliceMin(), panel0.getSliceMax(), currentSlice0);
		sliceSlider0.addChangeListener(this);
		sliceSlider1 = new JSlider(JSlider.HORIZONTAL, panel1.getSliceMin(), panel1.getSliceMax(), currentSlice1);
		sliceSlider1.addChangeListener(this);
		sliceSlider2 = new JSlider(JSlider.HORIZONTAL, panel2.getSliceMin(), panel2.getSliceMax(), currentSlice2);
		sliceSlider2.addChangeListener(this);
		sliderPanel.add(sliceSlider0, "wrap");
		sliderPanel.add(sliceSlider1, "wrap");
		sliderPanel.add(sliceSlider2, "wrap");

		JPanel filterPanel = new JPanel(new MigLayout());
		filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));
		buttonFilterTreshold = new JButton("Treshold Filter");
		buttonFilterTreshold.addActionListener(this);
		filterPanel.add(buttonFilterTreshold);
		buttonFilterMedian = new JButton("Median Filter");
		buttonFilterMedian.addActionListener(this);
		filterPanel.add(buttonFilterMedian, "wrap");
		buttonFilterGradient = new JButton("Gradient Filter");
		buttonFilterGradient.addActionListener(this);
		filterPanel.add(buttonFilterGradient);
		buttonExport = new JButton("Export as DICOM");
		buttonExport.addActionListener(this);
		filterPanel.add(buttonExport);

		controlsPanel.add(sliderPanel, "grow, wrap");
		controlsPanel.add(filterPanel);

		content.add(panel0, "grow");
		content.add(panel1, "grow, wrap");
		content.add(panel2, "grow");
		content.add(controlsPanel, "grow, wrap");

		add(content, BorderLayout.NORTH);
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
			if (crosshairsFlag) { panel0.setInputData(currentImageData_backup); }
			panel0.setSlice(currentSlice0);
		} else if (e.getSource().equals(sliceSlider1)) {
			currentSlice1 = sliceSlider1.getValue();
			if (crosshairsFlag) { panel1.setInputData(currentImageData_backup); } 
			panel1.setSlice(currentSlice1);
		} else if (e.getSource().equals(sliceSlider2)) {
			currentSlice2 = sliceSlider2.getValue();
			if (crosshairsFlag) { panel2.setInputData(currentImageData_backup); } 
			panel2.setSlice(currentSlice2);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(buttonFilterTreshold)) {
			ThresholdFilter threshold = new ThresholdFilter();
			this.crosshairsFlag = threshold.setAllSlices(false);
			threshold.setUpperThreshold(1000.0);
			threshold.setLowerThreshold(500.0);
			threshold.setSlice(panel0.getSlice(), panel1.getSlice(), panel2.getSlice());
			applyFilter(threshold);
		} else if (e.getSource().equals(buttonFilterMedian)) {
			MedianFilter median = new MedianFilter();
			median.SetKernelSize(3, 3, 3); 
			this.crosshairsFlag = median.setAllSlices(false);
			median.setSlice(panel0.getSlice(), panel1.getSlice(), panel2.getSlice());
			applyFilter(median);
		} else if (e.getSource().equals(buttonFilterGradient)) {
			GradientFilter gradient = new GradientFilter();
			this.crosshairsFlag = gradient.setAllSlices(false);
			gradient.setFilter("Roberts");
			gradient.setSlice(panel0.getSlice(), panel1.getSlice(), panel2.getSlice());
			applyFilter(gradient);
		} else if (e.getSource().equals(buttonExport)) {
			exportCurrentImage();
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
				String message = String.format("Completed %d of 166 slices.\n", progress);
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

		progressMonitor = new ProgressMonitor(this, "Exporting current image to DICOM", "Completed 0 of 166 slices.\n",
				0, 166);
		progressMonitor.setProgress(0);
		buttonExport.setEnabled(false);
		ExportTask task = new ExportTask();
		task.execute();
	}
}
