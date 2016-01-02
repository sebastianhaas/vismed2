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

	public ImageViewerPanel(vtkImageData imageData) {
		setLayout(new BorderLayout());
		imageViewer = new VtkImageViewer2Java(imageData);
		add(imageViewer, BorderLayout.CENTER);
	}

	public VtkImageViewer2Java getImageViewer() {
		return imageViewer;
	}

	public void setSlice(int slice) {
		imageViewer.GetVtkImageViewer().SetSlice(slice);
	}

	public int getSliceOrientation() {
		return imageViewer.GetVtkImageViewer().GetSliceOrientation();
	}

	public int getSliceMax() {
		return imageViewer.GetVtkImageViewer().GetSliceMax();
	}

	public int getSliceMin() {
		return imageViewer.GetVtkImageViewer().GetSliceMin();
	}

	public int getSlice() {
		return imageViewer.GetVtkImageViewer().GetSlice();
	}
	
	public void setInputData(vtkImageData data) {
		imageViewer.GetVtkImageViewer().SetInputData(data);
		imageViewer.GetVtkImageViewer().UpdateDisplayExtent();
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