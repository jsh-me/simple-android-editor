package kr.co.jsh.feature.storage

import kr.co.jsh.base.storage.BaseStoragePresenter
import kr.co.jsh.base.storage.BaseStorageView

interface StorageContract {
    interface View: BaseStorageView<Presenter> {
        fun refreshView(list: ArrayList<List<String>>)
        fun isEnd(b: Boolean)
    }
    interface Presenter: BaseStoragePresenter{
        var view: View
        fun isAnyMoreNoData()
    }
}