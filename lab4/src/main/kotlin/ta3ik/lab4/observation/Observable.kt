package ta3ik.lab4.observation

interface Observable {

    fun addObserver(observer: Observer?)
    fun notifyObservers()
}