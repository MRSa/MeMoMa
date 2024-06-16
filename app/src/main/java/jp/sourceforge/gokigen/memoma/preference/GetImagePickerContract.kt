package jp.sourceforge.gokigen.memoma.preference

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import androidx.activity.result.contract.ActivityResultContracts

class GetImagePickerContract: ActivityResultContracts.GetContent()
{
    override fun createIntent(context: Context, input: String): Intent
    {
        super.createIntent(context, input)

        val openRequestIntent = Intent(ACTION_OPEN_DOCUMENT)
        openRequestIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        openRequestIntent.addCategory(Intent.CATEGORY_OPENABLE)
        openRequestIntent.type = "image/*"
        //openRequestIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)

        return (openRequestIntent)
    }


}