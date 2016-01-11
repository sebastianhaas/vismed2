package vismed2.group3.filters;

import vismed2.group3.VisMedVTK;
import vtk.vtkImageData;

/**
 * 
 * @author Sebastian Haas
 * @author Alexander Tatowsky
 * <br>
 * 
 * The Maximum Intensity Projection searches with scanlines along 
 * the orthogonal axis for the highest intensity on the scanline. 
 * This projection mexes the information about depth, so it is no 
 * longer possible to differ between layers on the scanline. 
 * 
 * Wit X-Rays however it is a good and simple method to detect bones, 
 * since bones have a much higher intensity as soft tisue. 
 * Also it is used for example for the detection of lung nodules in 
 * lung cancer screening programs which utilise computed tomography scans.
 */
public class MIP implements VtkJavaFilter {

	private final vtkImageData out;
	private int sliceAlong_X;
	private int sliceAlong_Y;
	private int sliceAlong_Z;

	public MIP() {
		out = new vtkImageData();
	}

	@Override
	public void applyFilter(vtkImageData imgData) {
		// Prepare output data
		int[] dims = imgData.GetDimensions();
		out.Initialize();
		out.CopyStructure(imgData);
		out.CopyAttributes(imgData);
		out.DeepCopy(imgData);
		double pixelValue;
		int progress = 0;

		// scanline along X
		for (int width = 0; width < dims[1]; width++) {
			progress = (int) ((double) 100 / dims[1] * width);
			VisMedVTK.setStatusBar("Applying MIP. Progress: " + progress + " %");
			for (int height = 0; height < dims[0]; height++) {
				for (int slice = 0; slice < dims[2]; slice++) {
					// scanline along X
					pixelValue = imgData.GetScalarComponentAsDouble(height, width, slice, 0);
					if (pixelValue > out.GetScalarComponentAsDouble(height, width, sliceAlong_X, 0)) {
						out.SetScalarComponentFromDouble(height, width, sliceAlong_X, 0, pixelValue);
					}
					// scanline along Y
					if (pixelValue > out.GetScalarComponentAsDouble(height, sliceAlong_Y, slice, 0)) {
						out.SetScalarComponentFromDouble(height, sliceAlong_Y, slice, 0, pixelValue);
					}
					// scanline along Z
					if (pixelValue > out.GetScalarComponentAsDouble(sliceAlong_Z, width, slice, 0)) {
						out.SetScalarComponentFromDouble(sliceAlong_Z, width, slice, 0, pixelValue);
					}
				}
			}
		}
	}

	/**
	 * Give information about the active slices. Active slices are the slices
	 * which are shown at the moment of pressing the filter button.
	 * 
	 * @param sliceYZ
	 *            - along the X achsis
	 * @param sliceXZ
	 *            - along the Y achsis
	 * @param sliceYX
	 *            - along the Z achsis
	 */
	public void setSlice(int sliceYZ, int sliceXZ, int sliceYX) {
		this.sliceAlong_X = sliceYZ;
		this.sliceAlong_Y = sliceXZ;
		this.sliceAlong_Z = sliceYX;
	}

	@Override
	public String getFilterName() {
		return "Maximum Intensity Projection";
	}

	@Override
	public vtkImageData GetOutput() {
		return out;
	}

}
