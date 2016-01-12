package vismed2.group3.filters;

import java.util.Arrays;

import vismed2.group3.VisMedVTK;
import vtk.vtkImageData;

/**
 * @author Sebastian Haas
 * @author Alexander Tatowsky <br>
 *         The median filter creates a cube in the three dimensional space and
 *         convolutes it over a dicom dataset. This can be applied over the
 *         active slices or all slices of the data set by setting the boolean
 *         'setAllSlices(boolean doAllSlices)'. The convolution wors as follows:
 *         <ul>
 *         <li>Convolute the cube over the pictuure</li>
 *         <li>Fill the values of the picture into the cube</li>
 *         <li>sort the values in the cube</li>
 *         <li>determine the median of all pixel values</li>
 *         <li>set the center of the kernel to the kernel's median</li>
 *         </ul>
 * 
 *         The median filter is mainly used for noise reduction. Noise like salt
 *         & pepper can reduced very well but the image is softened and looses
 *         detail.
 * 
 *         The functionality of this filter is only guaranteed for grayvalue
 *         dicom data.
 */
public class MedianFilter implements VtkJavaFilter {

	private final vtkImageData out;
	private int filter_height;
	private int filter_width;
	private int filter_depth;
	private int sliceAlong_X = 0;
	private int sliceAlong_Y = 0;
	private int sliceAlong_Z = 0;
	private boolean doAllSlices = false;

	public MedianFilter() {
		out = new vtkImageData();
	}

	/**
	 * Set the size of the kernel. Although it is possible to set a even kernel
	 * size, an uneven kernel size makes more sense (especially with small
	 * kernels) since there is no exact center of the kernel at an uneven
	 * number.
	 * 
	 * @param height
	 * @param width
	 * @param depth
	 */
	public void SetKernelSize(int height, int width, int depth) {
		this.filter_height = height;
		this.filter_width = width;
		this.filter_depth = depth;
	}

	/**
	 * Filter the image with the set Kernel. This can either be applied to the
	 * active orthogonal slices or to the whole dataset by setting the boolean
	 * doAllSlices.
	 */
	@Override
	public void applyFilter(vtkImageData imgData) {
		// Prepare output data
		int[] dims = imgData.GetDimensions();
		out.Initialize();
		out.CopyStructure(imgData);
		out.CopyAttributes(imgData);
		out.DeepCopy(imgData);

		// do actual filtering
		double[] kernel = new double[filter_height * filter_width * filter_depth];
		double pixelValue;
		int progress = 0;

		if (doAllSlices) {
			// iterate through the image/ through all slices
			for (int slice = 0; slice < dims[2] - filter_depth; slice++) {
				// show progress at GUI
				progress = (int) ((double) 100 / dims[1] * slice);
				VisMedVTK.setStatusBar("Applying Median Filter. Progress: " + progress + " %");

				for (int width = filter_width; width < dims[1] - filter_width; width++) {
					for (int height = filter_height; height < dims[0] - filter_height; height++) {

						// fill values into kernel
						int count = 0;
						for (int x = slice - filter_depth / 2; x < slice + filter_depth / 2; x++) {
							for (int y = width - filter_width; y < width; y++) {
								for (int z = height - filter_height; z < height; z++) {
									// clipping
									if (x < 0) { // kernel < first Slice
										pixelValue = imgData.GetScalarComponentAsDouble(z, y, 0, 0);
									} // kernel > last slice
									else if (x > dims[1]) {
										int lastSlice = sliceAlong_X - (sliceAlong_X - dims[2]);
										pixelValue = imgData.GetScalarComponentAsDouble(z, y, lastSlice, 0);
									} else {
										pixelValue = imgData.GetScalarComponentAsDouble(z, y, x, 0);
									}
									kernel[count++] = pixelValue;
								}
							}
						} // get medial by sorting the list of values and taking
							// the one in the middle
						Arrays.sort(kernel);
						if (kernel.length % 2 == 0) { // even kernel size
							pixelValue = ((kernel[kernel.length / 2 - 1]) + kernel[kernel.length / 2]) / 2;
						} else { // uneven kernel size
							pixelValue = kernel[kernel.length / 2];
						}
						out.SetScalarComponentFromDouble(height, width, slice, 0, pixelValue);
					}
				}
			}
		} else { // do only active slices
			for (int slice = 0; slice < dims[2]; slice++) {
				progress = (int) ((double) 100 / dims[1] * slice);
				VisMedVTK.setStatusBar("Applying Median Filter. Progress: " + progress + " %");
				for (int width = filter_width; width < dims[1]; width++) {
					for (int height = filter_height; height < dims[0]; height++) {

						if (slice == sliceAlong_X) { // along X
							// fill values into kernel
							int count = 0;
							for (int x = sliceAlong_X - filter_depth / 2; x < sliceAlong_X + filter_depth / 2; x++) {
								for (int y = width - filter_width; y < width; y++) {
									for (int z = height - filter_height; z < height; z++) {
										// clipping
										if (x < 0) { // kernel < first Slice
											pixelValue = imgData.GetScalarComponentAsDouble(z, y, 0, 0);
										} // kernel > last slice
										else if (x > dims[1]) {
											int lastSlice = sliceAlong_X - (sliceAlong_X - dims[2]);
											pixelValue = imgData.GetScalarComponentAsDouble(z, y, lastSlice, 0);
										} else {
											pixelValue = imgData.GetScalarComponentAsDouble(z, y, x, 0);
										}
										kernel[count++] = pixelValue;
									}
								}
							}
							Arrays.sort(kernel);
							if (kernel.length % 2 == 0) { // even kernel size
								pixelValue = ((kernel[kernel.length / 2 - 1]) + kernel[kernel.length / 2]) / 2;
							} else { // uneven kernel size
								pixelValue = kernel[kernel.length / 2];
							}
							out.SetScalarComponentFromDouble(height, width, sliceAlong_X, 0, pixelValue);
						}

						if (width == sliceAlong_Y) { // along Y
							// already done it at "along X"
							if (slice != sliceAlong_X) {
								// fill values into kernel
								int count = 0;
								for (int x = slice - filter_depth / 2; x < slice + filter_depth / 2; x++) {
									for (int y = width - filter_width; y < width; y++) {
										for (int z = height - filter_height; z < height; z++) {
											// clipping
											if (x < 0) { // kernel < first Slice
												pixelValue = imgData.GetScalarComponentAsDouble(z, y, 0, 0);
											} else if (x > dims[1]) { // kernel
																		// >
																		// last
																		// Slice
												int lastSlice = slice - (slice - dims[2]);
												pixelValue = imgData.GetScalarComponentAsDouble(z, y, lastSlice, 0);
											} else {
												pixelValue = imgData.GetScalarComponentAsDouble(z, y, x, 0);
											}
											kernel[count++] = pixelValue;
										}
									}
								}
								Arrays.sort(kernel);
								if (kernel.length % 2 == 0) { // even kernel
																// size
									pixelValue = ((kernel[kernel.length / 2 - 1]) + kernel[kernel.length / 2]) / 2;
								} else { // uneven kernel size
									pixelValue = kernel[kernel.length / 2];
								}
								out.SetScalarComponentFromDouble(height, sliceAlong_Y, slice, 0, pixelValue);
							}

						}
						if (height == sliceAlong_Z) { // along Z
							// already done it at "along X" and "along Y"
							if (slice != sliceAlong_X && width != sliceAlong_Y) {
								// fill values into kernel
								int count = 0;
								for (int x = slice - filter_depth / 2; x < slice + filter_depth / 2; x++) {
									for (int y = width - filter_width; y < width; y++) {
										for (int z = height - filter_height; z < height; z++) {
											// clipping
											if (x < 0) { // kernel < first Slice
												pixelValue = imgData.GetScalarComponentAsDouble(z, y, 0, 0);
											} else if (x > dims[1]) { // kernel
																		// >
																		// last
																		// Slice
												int lastSlice = slice - (slice - dims[2]);
												pixelValue = imgData.GetScalarComponentAsDouble(z, y, lastSlice, 0);
											} else {
												pixelValue = imgData.GetScalarComponentAsDouble(z, y, x, 0);
											}
											kernel[count++] = pixelValue;
										}
									}
								}
								Arrays.sort(kernel);
								if (kernel.length % 2 == 0) { // even kernel
																// size
									pixelValue = ((kernel[kernel.length / 2 - 1]) + kernel[kernel.length / 2]) / 2;
								} else { // uneven kernel size
									pixelValue = kernel[kernel.length / 2];
								}
								out.SetScalarComponentFromDouble(sliceAlong_Z, width, slice, 0, pixelValue);
							}

						}
					}
				}
			}
		}
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
		return "Median";
	}

	@Override
	public vtkImageData GetOutput() {
		return out;
	}
}
