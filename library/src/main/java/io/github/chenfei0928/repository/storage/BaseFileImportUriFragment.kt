package io.github.chenfei0928.repository.storage

import android.content.Context
import android.net.Uri

/**
 * 文件导入处理，返回文件的uri，可能需要使用[Context.getContentResolver]来读取
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-19 12:49
 */
typealias BaseFileImportUriFragment = BaseFileImportFragment<Uri>
