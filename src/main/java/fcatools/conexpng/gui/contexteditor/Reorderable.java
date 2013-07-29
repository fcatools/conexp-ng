package fcatools.conexpng.gui.contexteditor;

/**
 * Needed for reordering objects/attributes when dragging and why this interface? -> reduce coupling of MatrixModel
 * and ContextMatrix.
 */
public interface Reorderable {

    public void reorderRows(int from, int to);

    public void reorderColumns(int from, int to);

}
