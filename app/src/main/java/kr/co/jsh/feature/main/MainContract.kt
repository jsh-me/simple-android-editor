package kr.co.jsh.feature.main

import kr.co.jsh.base.storage.BaseStoragePresenter
import kr.co.jsh.base.storage.BaseStorageView

interface MainContract{
    interface View : BaseStorageView<Presenter> {
        fun refreshView(list: ArrayList<List<String>>)
    }
    interface Presenter: BaseStoragePresenter {
        var view: View
        fun insertResultToLocalDB(list: ArrayList<List<String>>)
    }
}