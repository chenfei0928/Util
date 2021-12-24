package io.github.chenfei0928.base.mvp

internal interface PresenterContainer {
    fun onBindPresenter(presenter: BasePresenter<*>)
}
