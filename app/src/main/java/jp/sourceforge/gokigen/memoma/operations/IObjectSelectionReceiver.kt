package jp.sourceforge.gokigen.memoma.operations

/**
 * オブジェクトが選択されたことを通知するインタフェース
 */
interface IObjectSelectionReceiver
{
    fun touchedVacantArea(): Int
    fun touchUppedVacantArea(): Int
    fun objectCreated()
    fun objectSelected(key: Int?): Boolean
    fun objectSelectedContext(key: Int?)
}