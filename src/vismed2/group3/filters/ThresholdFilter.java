package vismed2.group3.filters;

import vtk.vtkImageData;

/**
 * The ThresholdFilter sets all pixels below the threshold as background, all
 * pixels above stay are given the original value
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

	@Override
	public void applyFilter(vtkImageData imgData) {
		// Prepare output data
		int[] dims = imgData.GetDimensions();
		out.Initialize();
		out.CopyStructure(imgData);
		out.CopyAttributes(imgData);
		out.DeepCopy(imgData);
		double pixelValue;

		// iterate over all slices and set every pixel 0 which isn't bigger than
		// the threshold
		if (doAllSlices) {
			for (int slice = 0; slice < dims[2]; slice++) {
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

	public boolean setAllSlices(boolean doAllSlices) {
		this.doAllSlices = doAllSlices;
		if (doAllSlices) return false;
		else return true;
	}

	public void setSlice(int sliceYZ, int sliceXZ, int sliceYX) {
		this.sliceAlong_X = sliceYZ;
		this.sliceAlong_Y = sliceXZ;
		this.sliceAlong_Z = sliceYX;
	}

	public void setUpperThreshold(double threshold) {
		this.upperThreshold = threshold;
	}

	public void setLowerThreshold(double threshold) {
		this.lowerThreshold = threshold;
	}

	@Override
	public String getFilterName() {
		return "Threshold";
	}

	@Override
	public vtkImageData GetOutput() {
		return out;
	}

}
