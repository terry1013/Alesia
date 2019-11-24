package delete;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.*;

public class DualAxisDemo51 extends CategoryPlot {
	public DualAxisDemo51(CategoryDataset paramCategoryDataset, CategoryAxis paramCategoryAxis, ValueAxis paramValueAxis,
			CategoryItemRenderer paramCategoryItemRenderer) {
		super(paramCategoryDataset, paramCategoryAxis, paramValueAxis, paramCategoryItemRenderer);
	}

	public LegendItemCollection getLegendItems() {
		LegendItemCollection localLegendItemCollection = new LegendItemCollection();
		CategoryDataset localCategoryDataset = getDataset();
		Object localObject2;
		if (localCategoryDataset != null) {
			CategoryItemRenderer localObject1 = getRenderer();
			if (localObject1 != null) {
				localObject2 = ((CategoryItemRenderer) localObject1).getLegendItem(0, 0);
				localLegendItemCollection.add((LegendItem) localObject2);
			}
		}
		Object localObject1 = getDataset(0);
		if (localObject1 != null) {
			localObject2 = getRenderer(0);
			if (localObject2 != null) {
				LegendItem localLegendItem = ((CategoryItemRenderer) localObject2).getLegendItem(0, 0);
				localLegendItemCollection.add(localLegendItem);
			}
		}
		return localLegendItemCollection;
	}
}
