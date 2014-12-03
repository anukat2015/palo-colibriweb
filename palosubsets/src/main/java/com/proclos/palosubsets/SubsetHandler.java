/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.proclos.palosubsets;

import com.jedox.palojlib.interfaces.ICell;
import com.jedox.palojlib.interfaces.ICellExportContext;
import com.jedox.palojlib.interfaces.ICellsExporter;
import com.jedox.palojlib.interfaces.ICube;
import com.jedox.palojlib.interfaces.IDatabase;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IElement;
import com.jedox.palojlib.main.CellExportContext;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author chris
 */
public class SubsetHandler {

    private static final String LOCAL = "#_SUBSET_LOCAL";
    private static final String GLOBAL = "#_SUBSET_GLOBAL";

    private static List<Subset2> loadSubsets(IDatabase database, IDimension dimension, String username, String cubeName) {
        ICellExportContext exContext = new CellExportContext(ICube.CellsExportType.BOTH, 10000, true, false, false);
        ICube cube = database.getCubeByName(cubeName);
        IElement[][] elems = new IElement[cube.getDimensions().length][];
        ICellsExporter result = cube.getCellsExporter(elems, exContext);
        ICell         exCell;
        List<Subset2> subsets = new ArrayList<Subset2>();

        while (result.hasNext()) {
            exCell = result.next();
            String[] cellPath = exCell.getPathNames();
            if (cellPath != null && cellPath.length >= 1) {
                if (cellPath[0].equals(dimension.getName()) && (username == null || username.equals(cellPath[1]))) {
                    String subsetName = cellPath[1];
                    String definition = exCell.getValue().toString();
                    Subset2 subset = new SubsetImpl(subsetName, database, definition, Subset2.TYPE_GLOBAL, username);
                    subsets.add(subset);
                }
            }
        }
        return subsets;
    }

    public static Subset2[] loadSubsets(IDatabase database, IDimension dimension, String username) {
        List<Subset2> subsets = new ArrayList<Subset2>();
        /*deavtivated work in progress
        subsets.addAll(loadSubsets(database, dimension, null, GLOBAL));
        subsets.addAll(loadSubsets(database, dimension, username, LOCAL));
         *
         */
        return subsets.toArray(new Subset2[subsets.size()]);
    }

    public static Subset2 createSubset(IDatabase database, IDimension dimension, int type, String username) {
        return new SubsetImpl(database, dimension, type, username);
    }

    public static void saveSubset(Subset2 subset) {
        IDatabase database = subset.getDatabase();
        IDimension subsetDim = database.getDimensionByName("#_SUBSET_");
        IElement subsetElement = subsetDim.getElementByName(subset.getName(), false);
        if (subsetElement == null || subset.getId() == null) { //new subset
            //determine id
            subsetElement = subsetDim.addBaseElement(subset.getName(), IElement.ElementType.ELEMENT_NUMERIC);
        }
        subset.setId(Integer.valueOf(((com.jedox.palojlib.main.Element)subsetElement).getId()).longValue());
        IDimension dimensionDim = database.getDimensionByName("#_Dimension_");
        IElement dimElement = dimensionDim.getElementByName(subset.getDimension().getName(), false);
        String cubeName = subset.getType() == Subset2.TYPE_LOCAL ? LOCAL : GLOBAL;
        ICube cube = subset.getDatabase().getCubeByName(cubeName);
        subset.save();
        Object[] values = new Object[] {subset.getDefinition()};
        if (subset.getType() == Subset2.TYPE_GLOBAL) {
            IElement[][] elements = new IElement[1][2];
            elements[0][0] = dimElement;
            elements[0][1] = subsetElement;
            cube.loadCells(elements, values,cube.getCellLoadContext(ICube.SplashMode.SPLASH_NOSPLASHING, 1, false, false),null);
        } else {
            IElement[][] elements = new IElement[1][3];
            elements[0][0] = dimElement;
            elements[0][1] = database.getDimensionByName("#_USER").getElementByName(subset.getUsername(), false);
            elements[0][2] = subsetElement;
            cube.loadCells(elements, values,cube.getCellLoadContext(ICube.SplashMode.SPLASH_NOSPLASHING, 1, false, false),null);
        }


    }

}
