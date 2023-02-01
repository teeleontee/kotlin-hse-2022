package binomial

import java.util.NoSuchElementException

/*
 * FList - реализация функционального списка
 *
 * Пустому списку соответствует тип Nil, непустому - Cons
 *
 * Запрещено использовать
 *
 *  - var
 *  - циклы
 *  - стандартные коллекции
 *
 *  Исключение Array-параметр в функции flistOf. Но даже в ней нельзя использовать цикл и forEach.
 *  Только обращение по индексу
 */


sealed class FList<T>: Iterable<T> {

    abstract val head: T?

    abstract val tail: FList<T>

    internal class FListIterator<T>(var flist : FList<T>) : Iterator<T> {
        override fun hasNext(): Boolean = !flist.isEmpty

        override fun next(): T = if (!hasNext()) throw NoSuchElementException("Empty") else {
            val ans = flist.head
            flist = flist.tail
            ans!!
        }
    }

    // размер списка, 0 для Nil, количество элементов в цепочке для Cons
    abstract val size: Int
    // пустой ли списк, true для Nil, false для Cons
    abstract val isEmpty: Boolean

    // получить список, применив преобразование
    // требуемая сложность - O(n)
    abstract fun <U> map(f: (T) -> U): FList<U>

    // получить список из элементов, для которых f возвращает true
    // требуемая сложность - O(n)
    abstract fun filter(f: (T) -> Boolean): FList<T>

    // свертка
    // требуемая сложность - O(n)
    // Для каждого элемента списка (curr) вызываем f(acc, curr),
    // где acc - это base для начального элемента, или результат вызова
    // f(acc, curr) для предыдущего
    // Результатом fold является результат последнего вызова f(acc, curr)
    // или base, если список пуст
    abstract fun <U> fold(base: U, f: (U, T) -> U): U

    // разворот списка
    // требуемая сложность - O(n)
    fun reverse(): FList<T> = fold<FList<T>>(nil()) { acc, current ->
        Cons(current, acc)
    }

    /*
     * Это не очень красиво, что мы заводим отдельный Nil на каждый тип
     * И вообще лучше, чтобы Nil был объектом
     *
     * Но для этого нужны приседания с ковариантностью
     *
     * dummy - костыль для того, что бы все Nil-значения были равны
     *         и чтобы Kotlin-компилятор был счастлив (он требует, чтобы у Data-классов
     *         были свойство)
     *
     * Также для борьбы с бойлерплейтом были введены функция и свойство nil в компаньоне
     */
    override fun iterator(): Iterator<T> = FListIterator(this)

    data class Nil<T>(private val dummy: Int=0) : FList<T>() {
        override val head: T? = null

        override val tail: FList<T>
            get() = nil()

        override val size: Int = 0

        override val isEmpty: Boolean = true

        override fun <U> fold(base: U, f: (U, T) -> U): U = base

        override fun filter(f: (T) -> Boolean): FList<T> = nil()

        override fun <U> map(f: (T) -> U): FList<U> = nil()
    }

    data class Cons<T>(override val head: T, override val tail: FList<T>) : FList<T>() {
        override val size: Int = tail.size + 1
        override val isEmpty: Boolean = false

        override fun <U> fold(base: U, f: (U, T) -> U): U {
            tailrec fun <U> folder(base: U, f: (U, T) -> U, it: FListIterator<T>): U {
                return if (!it.hasNext())
                    base
                else
                    folder(f(base, it.next()), f, it)
            }
            return folder(base, f, FListIterator(this))
        }
        override fun filter(f: (T) -> Boolean): FList<T> {
            tailrec fun filterThrough(f: (T) -> Boolean, cur: FList<T>, it: FListIterator<T>): FList<T> {
                return if (!it.hasNext())
                    cur
                else {
                    val head = it.next()
                    if (f(head))
                        filterThrough(f, Cons(head, cur), it)
                    else {
                        filterThrough(f, cur, it)
                    }
                }
            }
            return filterThrough(f, nil(), FListIterator(this))
        }
        override fun <U> map(f: (T) -> U): FList<U> {
            tailrec fun mapper(f: (T) -> U, cur: FList<U>, it: FListIterator<T>): FList<U> {
                return if (!it.hasNext())
                    cur
                else {
                    val head = it.next()
                    mapper(f, Cons(f(head), cur), it)
                }
            }
            return mapper(f, nil(), FListIterator(reverse()))
        }
    }

    companion object {
        fun <T> nil() = Nil<T>()
        val nil = Nil<Any>()
    }
}

internal tailrec fun <T> helpFlistOf(index: Int, cur: FList<T>, vararg list: T): FList<T> {
    return if (index == list.size) {
        cur
    } else {
        helpFlistOf(index + 1, FList.Cons(list[index], cur), *list)
    }
}
// конструирование функционального списка в порядке следования элементов
// требуемая сложность - O(n)
fun <T> flistOf(vararg values: T): FList<T> = helpFlistOf(0, FList.Nil<T>(), *values)
