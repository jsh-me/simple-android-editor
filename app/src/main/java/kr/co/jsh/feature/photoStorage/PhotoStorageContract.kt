package kr.co.jsh.feature.photoStorage

import kr.co.jsh.base.storage.BaseStoragePresenter
import kr.co.jsh.base.storage.BaseStorageView

interface PhotoStorageContract {
    interface View: BaseStorageView<Presenter>
    interface Presenter: BaseStoragePresenter {
        var view: View
    }
}