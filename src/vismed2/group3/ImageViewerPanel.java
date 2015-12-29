package vismed2.group3;

import java.awt.BorderLayout;

import javax.swing.JComponent;

import vtk.vtkCanvas;
import vtk.vtkImageData;
import vtk.vtkImageViewer2;

public class ImageViewerPanel extends JComponent {

	public static final int ORIENTATION_XY = 2;
	public static final int ORIENTATION_XZ = 1;
	public static final int ORIENTATION_YZ = 0;
	private static final long serialVersionUID = -6951851465094754751L;
	private VtkImageViewer2Java imageViewer;
	private int orientation;

	public ImageViewerPanel(int orientation, vtkImageData imageData) {
		setLayout(new BorderLayout());
		this.orientation = orientation;
		imageViewer = new VtkImageViewer2Java(imageData);
		add(imageViewer, BorderLayout.CENTER);

		Runnable r1 = new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
					initialize();
				} catch (InterruptedException iex) {
				}
			}
		};
		Thread thr1 = new Thread(r1);
		thr1.start();
	}

	// Has to be called when render windows are attached to the Swing container
	private void initialize() {
		switch (orientation) {
		case ORIENTATION_XY:
			imageViewer.GetVtkImageViewer().SetSliceOrientationToXY();
		case ORIENTATION_XZ:
			imageViewer.GetVtkImageViewer().SetSliceOrientationToXZ();
		case ORIENTATION_YZ:
			imageViewer.GetVtkImageViewer().SetSliceOrientationToYZ();
		default:
			imageViewer.GetVtkImageViewer().SetSliceOrientationToXY();
		}
		imageViewer.GetVtkImageViewer().SetSlice(imageViewer.GetVtkImageViewer().GetSliceMin());
		imageViewer.GetVtkImageViewer().Render();
	}

	public void setSlice(int slice) {
		imageViewer.GetVtkImageViewer().SetSlice(slice);
	}

	public int GetSliceOrientation() {
		return imageViewer.GetVtkImageViewer().GetSliceOrientation();
	}

	public int GetSliceMax() {
		return imageViewer.GetVtkImageViewer().GetSliceMax();
	}

	public int GetSliceMin() {
		return imageViewer.GetVtkImageViewer().GetSliceMin();
	}

	public int GetSlice() {
		return imageViewer.GetVtkImageViewer().GetSlice();
	}
}

class VtkImageViewer2Java extends vtkCanvas {

	private static final long serialVersionUID = 1L;

	private vtkImageViewer2 imageViewer;

	public VtkImageViewer2Java(vtkImageData imageData) {
		imageViewer = new vtkImageViewer2();
		imageViewer.SetInputData(imageData);
		imageViewer.SetRenderWindow(GetRenderWindow());
		imageViewer.SetRenderer(GetRenderer());
		imageViewer.SetupInteractor(getRenderWindowInteractor());
		imageViewer.UpdateDisplayExtent();
	}

	public void repaint() {
		imageViewer.UpdateDisplayExtent();
		imageViewer.Render();
		super.repaint();
	}

	public vtkImageViewer2 GetVtkImageViewer() {
		return imageViewer;
	}

}