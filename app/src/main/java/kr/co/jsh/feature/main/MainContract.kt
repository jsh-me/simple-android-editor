package kr.co.jsh.feature.main

import kr.co.jsh.base.storage.BaseStoragePresenter
import kr.co.jsh.base.storage.BaseStorageView

interface MainContract{
    interface View : BaseStorageView<Presenter>
    interface Presenter: BaseStoragePresenter {
        var view: View
    }
}