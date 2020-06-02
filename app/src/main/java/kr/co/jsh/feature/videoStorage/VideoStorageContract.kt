package kr.co.jsh.feature.videoStorage

import kr.co.jsh.base.storage.BaseStoragePresenter
import kr.co.jsh.base.storage.BaseStorageView

interface VideoStorageContract {
    interface View: BaseStorageView<Presenter>
    interface Presenter: BaseStoragePresenter{
        var view: View
    }
}