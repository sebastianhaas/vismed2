package vismed2.group3.filters;

import vismed2.group3.VisMedVTK;
import vtk.vtkImageData;

/**
 * @author Alexander Tatowsky
 * 
 * The ThresholdFilter sets all pixels outside of an Threshold- interval as background, all
 * pixels inside the interval stay the original value. 
 * The result can be shown only on the active slice or can be applied to all slices by 
 * setting the boolean doAllSlices to true. 
 * 
 * The functionality of this filter is only guaranteed for grayvalue dicom data.
 */
public class ThresholdFilter implements VtkJavaFilter {

	private final vtkImageData out;
	private double upperThreshold = 0;
	private double lowerThreshold = 0;
	private int sliceAlong_X = 0;
	private int sliceAlong_Y = 0;
	private int sliceAlong_Z = 0;
	private boolean doAllSlices = false;

	public ThresholdFilter() {
		out = new vtkImageData();
	}

	/**
	 * Apply the Threshold Filter. Pixels outside of the threshold- span are set to background. 
	 * Pixels inside of the threshold- interval stay the same. This can either be applied to 
	 * the active orthogonal slices or to the whole dataset by setting the boolean doAllSlices.
	 */
	@Override
	public void applyFilter(vtkImageData imgData) {
		// Prepare output data
		int[] dims = imgData.GetDimensions();
		out.Initialize();
		out.CopyStructure(imgData);
		out.CopyAttributes(imgData);
		out.DeepCopy(imgData);
		double pixelValue;
		int progress;
		
		// iterate over all slices and set every pixel 0 which isn't bigger than
		// the threshold
		if (doAllSlices) {
			for (int slice = 0; slice < dims[2]; slice++) {
				progress = (int)((double) 100/dims[1] * slice);
				VisMedVTK.setStatusBar("Applying Threshold Filter. Progress: " + progress + " %");
				for (int width = 0; width < dims[1]; width++) {
					for (int height = 0; height < dims[0]; height++) {
						pixelValue = imgData.GetScalarComponentAsDouble(height, width, slice, 0);
						if (pixelValue >= lowerThreshold && pixelValue <= upperThreshold) {
							out.SetScalarComponentFromDouble(height, width, slice, 0, pixelValue);
						} else {
							out.SetScalarComponentFromDouble(height, width, slice, 0, 0);
						}
					}
				}
			}
		} else {
			// iterate over relevant slices and set every pixel 0 which isn't
			// bigger than the threshold (YZ) -> Along X
			for (int slice = 0; slice < dims[2]; slice++) {
				progress = (int)((double) 100/dims[1] * slice);
				VisMedVTK.setStatusBar("Applying Threshold Filter. Progress: " + progress + " %");
				for (int width = 0; width < dims[1]; width++) {
					for (int height = 0; height < dims[0]; height++) {
						if (slice == sliceAlong_X) { // along X
							pixelValue = imgData.GetScalarComponentAsDouble(height, width, sliceAlong_X, 0);
							if (pixelValue >= lowerThreshold && pixelValue <= upperThreshold) {
								out.SetScalarComponentFromDouble(height, width, sliceAlong_X, 0, pixelValue);
							} else {
								out.SetScalarComponentFromDouble(height, width, sliceAlong_X, 0, 0);
							}
						}
						if (width == sliceAlong_Y) { // along Y
							pixelValue = imgData.GetScalarComponentAsDouble(height, sliceAlong_Y, slice, 0);
							if (pixelValue >= lowerThreshold && pixelValue <= upperThreshold) {
								out.SetScalarComponentFromDouble(height, sliceAlong_Y, slice, 0, pixelValue);
							} else {
								out.SetScalarComponentFromDouble(height, sliceAlong_Y, slice, 0, 0);
							}
						}
						if (height == sliceAlong_Z) { // along Z
							pixelValue = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width, slice, 0);
							if (pixelValue >= lowerThreshold && pixelValue <= upperThreshold) {
								out.SetScalarComponentFromDouble(sliceAlong_Z, width, slice, 0, pixelValue);
							} else {
								out.SetScalarComponentFromDouble(sliceAlong_Z, width, slice, 0, 0);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Set the flag whether all slices should be filtered or just the active slices 
	 * @param doAllSlices
	 * @return oposite of set boolean
	 */
	public boolean setAllSlices(boolean doAllSlices) {
		this.doAllSlices = doAllSlices;
		if (doAllSlices) return false;
		else return true;
	}

	/**
	 * Give information about the active slices. Active slices are the slices which 
	 * are shown at the moment of pressing the filter button. 
	 * 
	 * @param sliceYZ
	 * @param sliceXZ
	 * @param sliceYX
	 */
	public void setSlice(int sliceYZ, int sliceXZ, int sliceYX) {
		this.sliceAlong_X = sliceYZ;
		this.sliceAlong_Y = sliceXZ;
		this.sliceAlong_Z = sliceYX;
	}

	/**
	 * Set the upper limit if the threshold interval 
	 * @param threshold
	 */
	public void setUpperThreshold(double threshold) {
		this.upperThreshold = threshold;
	}

	/**
	 * Set the lower limit if the threshold interval 
	 * @param threshold
	 */
	public void setLowerThreshold(double threshold) {
		this.lowerThreshold = threshold;
	}

	/**
	 * Return the name of the filter
	 */
	@Override
	public String getFilterName() {
		return "Threshold";
	}

	/**
	 * return the result of the filter
	 */
	@Override
	public vtkImageData GetOutput() {
		return out;
	}

}
