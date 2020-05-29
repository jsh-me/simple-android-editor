package kr.co.domain.globalconst

//file pid save
object PidClass {
    var videoObjectPid = String()
    var videoMaskObjectPid = String()
    var imageObjectPid = String()
    var imageMaskObjectPid = String()

    //------서버로 가져올 파일의 pid 를 저장하는 것. 하지만 room setting이 되어서 쓸 일이 없을 듯.
    var topVideoObjectPid = ArrayList<String>()
    var topImageObjectPid = ArrayList<String>()
}