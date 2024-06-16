package jp.sourceforge.gokigen.memoma.operations;


/**
 *   オブジェクトが選択されたことを通知する
 * 
 * @author MRSa
 *
 */
public interface IObjectSelectionReceiver
{
	int touchedVacantArea();
	int touchUppedVacantArea();
	void objectCreated();
    boolean objectSelected(Integer key);
    void objectSelectedContext(Integer key);
}
