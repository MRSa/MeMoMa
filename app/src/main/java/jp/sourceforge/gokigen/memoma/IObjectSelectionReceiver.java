package jp.sourceforge.gokigen.memoma;


/**
 *   オブジェクトが選択されたことを通知する
 * 
 * @author MRSa
 *
 */
public interface IObjectSelectionReceiver
{
	public abstract int touchedVacantArea();
	public abstract int touchUppedVacantArea();
	public abstract void objectCreated();
    public abstract boolean objectSelected(Integer key);
    public abstract void objectSelectedContext(Integer key);
	
}
