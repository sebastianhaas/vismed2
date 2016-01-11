package vismed2.group3.filters;

import vismed2.group3.VisMedVTK;
import vtk.vtkImageData;

/**
 * @author Sebastian Haas
 * @author Alexander Tatowsky
 * <br>
 *         The gradient (=first order deviation) of a image intensity function
 *         is the difference between the intensity of two neighboring pixels.
 * 
 *         Depending on which direction the difference is measured there are
 *         different Kernels:
 * 
 *         GradientXY: Difference in the horizontal and vertical direction:
 * 
 *         <pre>
 * {@code
 *  1 -1        -1 -1
 *  1 -1   and   1  1
 * }
 *         </pre>
 * 
 *         Roberts' cross Operator:
 * 
 *         <pre>
 * {@code
 *  0 1 | 1 0 
 * -1 0 | 0 -1
 * }
 *         </pre>
 * 
 *         Sobel Filter:
 * 
 *         <pre>
 * {@code
 *  1 0 -1 |  1  2  1 
 *  2 0 -2 |  0  0  0 
 *  1 0 -1 | -1 -2 -1
 *}
 *         </pre>
 * 
 *         The functionality of this filter is only guaranteed for monochrome
 *         DICOM data.
 */
public class GradientFilter implements VtkJavaFilter {

	private final vtkImageData out;

	private Type filterType = Type.Roberts;
	private int sliceAlong_X = 0;
	private int sliceAlong_Y = 0;
	private int sliceAlong_Z = 0;
	private boolean doAllSlices = false;

	public enum Type {
		GradientXY, Roberts, Sobel
	};

	public GradientFilter() {
		out = new vtkImageData();
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
	public void applyFilter(vtkImageData imgData) {
		if (this.filterType.equals(Type.Roberts)) {
			doRoberts(imgData);
		} else if (filterType.equals(Type.Sobel)) {
			doSobel(imgData);
		} else if (filterType.equals(Type.GradientXY)) {
			doGradient(imgData);
		} else
			System.err.println("Unknown Gradient Algorithm!");
	}

	/**
	 * GradientXY: Difference in the horizontal and vertical direction:
	 * 
	 * <pre>
	 * {@code
	 *  1 -1        -1 -1
	 *  1 -1   and   1  1
	 * }
	 * </pre>
	 * 
	 * @param imgData
	 */
	private void doGradient(vtkImageData imgData) {
		// Prepare output data
		int[] dims = imgData.GetDimensions();
		out.Initialize();
		out.CopyStructure(imgData);
		out.CopyAttributes(imgData);
		out.DeepCopy(imgData);

		double[] pixelValue1 = new double[12];
		double pixelValue;
		int progress = 0;

		if (doAllSlices) { // iterate through the image/ through all slices
			System.err.println("Not available for this algorithm.");
		} else { // only do active slice
			for (int slice = 0; slice < dims[2]; slice++) {
				progress = (int) ((double) 100 / dims[1] * slice);
				VisMedVTK.setStatusBar(
						String.format("Applying %s Filter. Progress: %d%%", String.valueOf(filterType), progress));
				for (int width = 0; width < dims[1]; width++) {
					for (int height = 0; height < dims[0]; height++) {
						if (slice == sliceAlong_X) { // along X
							int value = 0;

							pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, width, sliceAlong_X, 0);
							pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height + 1, width, sliceAlong_X,
									0);
							pixelValue1[value++] = -imgData.GetScalarComponentAsDouble(height, width + 1, sliceAlong_X,
									0);
							pixelValue1[value++] = -imgData.GetScalarComponentAsDouble(height + 1, width + 1,
									sliceAlong_X, 0);

							pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, width, sliceAlong_X, 0);
							pixelValue1[value++] = -imgData.GetScalarComponentAsDouble(height + 1, width, sliceAlong_X,
									0);
							pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, width + 1, sliceAlong_X,
									0);
							pixelValue1[value++] = -imgData.GetScalarComponentAsDouble(height + 1, width + 1,
									sliceAlong_X, 0);

							double sumValues = 0;
							for (int i = 0; i < pixelValue1.length; i++) {
								sumValues += pixelValue1[i];
							}
							pixelValue = (sumValues) / 8;
							out.SetScalarComponentFromDouble(height, width, sliceAlong_X, 0, pixelValue);
						}
						if (width == sliceAlong_Y) {
							int value = 0;

							pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, width, slice + 1, 0);
							pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height + 1, width, slice + 1, 0);
							pixelValue1[value++] = -imgData.GetScalarComponentAsDouble(height, width, slice, 0);
							pixelValue1[value++] = -imgData.GetScalarComponentAsDouble(height + 1, width, slice, 0);

							pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, width, slice + 1, 0);
							pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, width, slice, 0);
							pixelValue1[value++] = -imgData.GetScalarComponentAsDouble(height + 1, width, slice + 1, 0);
							pixelValue1[value++] = -imgData.GetScalarComponentAsDouble(height + 1, width, slice, 0);

							double sumValues = 0;
							for (int i = 0; i < pixelValue1.length; i++) {
								sumValues += pixelValue1[i];
							}
							pixelValue = (sumValues) / 8;
							out.SetScalarComponentFromDouble(height, sliceAlong_Y, slice, 0, pixelValue);
						}
						if (height == sliceAlong_Z) {
							int value = 0;

							pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, width, slice, 0);
							pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, width, slice + 1, 0);
							pixelValue1[value++] = -imgData.GetScalarComponentAsDouble(height, width + 1, slice, 0);
							pixelValue1[value++] = -imgData.GetScalarComponentAsDouble(height, width + 1, slice + 1, 0);

							pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, width, slice, 0);
							pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, width + 1, slice, 0);
							pixelValue1[value++] = -imgData.GetScalarComponentAsDouble(height, width, slice + 1, 0);
							pixelValue1[value++] = -imgData.GetScalarComponentAsDouble(height, width + 1, slice + 1, 0);

							double sumValues = 0;
							for (int i = 0; i < pixelValue1.length; i++) {
								sumValues += pixelValue1[i];
							}
							pixelValue = (sumValues) / 8;
							out.SetScalarComponentFromDouble(sliceAlong_Z, width, slice, 0, pixelValue);
						}
					}
				}
			}
		}
	}

	/**
	 * Roberts' cross Operator:
	 * 
	 * <pre>
	 * {@code
	 *  0 1 | 1 0 
	 * -1 0 | 0 -1
	 * }
	 * </pre>
	 */
	private void doRoberts(vtkImageData imgData) {
		// Prepare output data
		int[] dims = imgData.GetDimensions();
		out.Initialize();
		out.CopyStructure(imgData);
		out.CopyAttributes(imgData);
		out.DeepCopy(imgData);

		double pixelValue1;
		double pixelValue2;
		double pixelValue3;
		double pixelValue4;
		double pixelValue;
		int progress = 0;

		if (doAllSlices) { // iterate through the image/ through all slices
			System.err.println("Not available for this algorithm.");
		} else { // only do active slice
			for (int slice = 0; slice < dims[2]; slice++) {
				progress = (int) ((double) 100 / dims[1] * slice);
				VisMedVTK.setStatusBar(
						String.format("Applying %s Filter. Progress: %d%%", String.valueOf(filterType), progress));
				for (int width = 0; width < dims[1]; width++) {
					for (int height = 0; height < dims[0]; height++) {
						if (slice == sliceAlong_X) { // along X
							pixelValue1 = imgData.GetScalarComponentAsDouble(height, width + 1, sliceAlong_X, 0);
							pixelValue2 = imgData.GetScalarComponentAsDouble(height + 1, width, sliceAlong_X, 0) * (-1);

							pixelValue3 = imgData.GetScalarComponentAsDouble(height, width, sliceAlong_X, 0);
							pixelValue4 = imgData.GetScalarComponentAsDouble(height + 1, width + 1, sliceAlong_X, 0)
									* (-1);

							pixelValue = (pixelValue1 + pixelValue2 + pixelValue3 + pixelValue4) / 8;
							out.SetScalarComponentFromDouble(height, width, sliceAlong_X, 0, pixelValue);
						}
						if (width == sliceAlong_Y) {
							pixelValue1 = imgData.GetScalarComponentAsDouble(height, sliceAlong_Y, slice, 0);
							pixelValue2 = imgData.GetScalarComponentAsDouble(height + 1, sliceAlong_Y, slice + 1, 0)
									* (-1);

							pixelValue3 = imgData.GetScalarComponentAsDouble(height + 1, sliceAlong_Y, slice, 0);
							pixelValue4 = imgData.GetScalarComponentAsDouble(height, sliceAlong_Y, slice + 1, 0) * (-1);

							pixelValue = (pixelValue1 + pixelValue2 + pixelValue3 + pixelValue4) / 8;
							out.SetScalarComponentFromDouble(height, sliceAlong_Y, slice, 0, pixelValue);
						}
						if (height == sliceAlong_Z) {
							pixelValue = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width, slice, 0);
							pixelValue1 = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width + 1, slice, 0);
							pixelValue2 = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width, slice + 1, 0) * (-1);

							pixelValue3 = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width, slice, 0);
							pixelValue4 = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width + 1, slice + 1, 0)
									* (-1);

							pixelValue = (pixelValue1 + pixelValue2 + pixelValue3 + pixelValue4) / 8;
							out.SetScalarComponentFromDouble(sliceAlong_Z, width, slice, 0, pixelValue);
						}
					}
				}
			}
		}
	}

	/**
	 * Sobel Filter:
	 * 
	 * <pre>
	 * {@code
	 *  1 0 -1 |  1  2  1 
	 *  2 0 -2 |  0  0  0 
	 *  1 0 -1 | -1 -2 -1
	 *}
	 * </pre>
	 */
	public void doSobel(vtkImageData imgData) {
		// Prepare output data
		int[] dims = imgData.GetDimensions();
		out.Initialize();
		out.CopyStructure(imgData);
		out.CopyAttributes(imgData);
		out.DeepCopy(imgData);

		double[] pixelValue1 = new double[12];
		double pixelValue;
		int progress = 0;

		for (int slice = 0; slice < dims[2] - 3; slice++) {
			progress = (int) ((double) 100 / dims[1] * slice);
			VisMedVTK.setStatusBar(
					String.format("Applying %s Filter. Progress: %d%%", String.valueOf(filterType), progress));
			for (int width = 0; width < dims[1] - 3; width++) {
				for (int height = 0; height < dims[0] - 3; height++) {
					if (slice == sliceAlong_X) { // along X
						int value = 0;
						// Kernel 1
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, width, sliceAlong_X, 0);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height + 1, width, sliceAlong_X, 0)
								* 2;
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height + 2, width, sliceAlong_X, 0);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, width + 2, sliceAlong_X, 0)
								* (-1);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height + 1, width + 2, sliceAlong_X,
								0) * (-2);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height + 2, width + 2, sliceAlong_X,
								0) * (-1);

						// Kernel 2
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, width, sliceAlong_X, 0);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, width + 1, sliceAlong_X, 0)
								* 2;
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, width + 2, sliceAlong_X, 0);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height + 2, width, sliceAlong_X, 0)
								* (-1);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height + 2 + 1, width + 1,
								sliceAlong_X, 0) * (-2);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height + 2 + 2, width + 2,
								sliceAlong_X, 0) * (-1);

						double sumValues = 0;
						for (int i = 0; i < pixelValue1.length; i++) {
							sumValues += pixelValue1[i];
						}
						pixelValue = (sumValues) / 18;
						out.SetScalarComponentFromDouble(height, width, sliceAlong_X, 0, pixelValue);
					}
					if (width == sliceAlong_Y) {
						int value = 0;
						// Kernel 1
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, sliceAlong_Y, slice + 2, 0);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height + 1, sliceAlong_Y, slice + 2,
								0) * 2;
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height + 2, sliceAlong_Y, slice + 2,
								0);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height + 2, sliceAlong_Y, slice, 0)
								* (-1);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height + 2, sliceAlong_Y, slice + 1,
								0) * (-2);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height + 2, sliceAlong_Y, slice + 2,
								0) * (-1);

						// Kernel 2
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, sliceAlong_Y, slice, 0);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, sliceAlong_Y, slice + 1, 0)
								* 2;
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height, sliceAlong_Y, slice + 2, 0);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height + 2, sliceAlong_Y, slice, 0)
								* (-1);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height + 2, sliceAlong_Y, slice + 1,
								0) * (-2);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(height + 2, sliceAlong_Y, slice + 2,
								0) * (-1);

						double sumValues = 0;
						for (int i = 0; i < pixelValue1.length; i++) {
							sumValues += pixelValue1[i];
						}
						pixelValue = (sumValues) / 18;
						out.SetScalarComponentFromDouble(height, sliceAlong_Y, slice, 0, pixelValue);
					}
					if (height == sliceAlong_Z) {
						int value = 0;
						// Kernel 1
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width, slice, 0);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width, slice + 1, 0)
								* 2;
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width, slice + 2, 0);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width + 2, slice, 0)
								* (-1);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width + 2, slice + 1, 0)
								* (-2);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width + 2, slice + 2, 0)
								* (-1);

						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width, slice, 0);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width + 1, slice, 0)
								* 2;
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width + 2, slice, 0);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width, slice + 2, 0)
								* (-1);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width + 1, slice + 2, 0)
								* (-2);
						pixelValue1[value++] = imgData.GetScalarComponentAsDouble(sliceAlong_Z, width + 2, slice + 2, 0)
								* (-1);

						double sumValues = 0;
						for (int i = 0; i < pixelValue1.length; i++) {
							sumValues += pixelValue1[i];
						}
						pixelValue = (sumValues) / 18;
						out.SetScalarComponentFromDouble(sliceAlong_Z, width, slice, 0, pixelValue);
					}
				}
			}
		}
	}

	/**
	 * set which type of gradient filter shall be applied. Possible filter types
	 * are: GradientXY, Roberts, Sobel.
	 * 
	 * @param filterType
	 */
	public void setFilter(Type filterType) {
		this.filterType = filterType;
	}

	/**
	 * Set the flag whether all slices should be filtered or just the active
	 * slices
	 * 
	 * @param doAllSlices
	 * @return oposite of set boolean
	 */
	public boolean setAllSlices(boolean doAllSlices) {
		this.doAllSlices = doAllSlices;
		if (doAllSlices)
			return false;
		else
			return true;
	}

	@Override
	public String getFilterName() {
		return String.valueOf(filterType);
	}

	@Override
	public vtkImageData GetOutput() {
		return out;
	}

}
