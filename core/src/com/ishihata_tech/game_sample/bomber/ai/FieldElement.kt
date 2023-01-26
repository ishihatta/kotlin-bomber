package com.ishihata_tech.game_sample.bomber.ai

data class FieldElement(
    val x: Int,
    val y: Int,
    var fieldObject: FieldObject = FieldObject.NONE,

    /**
     * この場所を通ることのリスク度合い
     */
    var risk: Int = 0,

    /**
     * この場所までの距離
     */
    var distance: Int = 1000,

    /**
     * この場所までのコスト
     */
    var cost: Int = 10000,

    /**
     * この場所にたどり着くための「前の場所」
     */
    var previousElement: FieldElement? = null,

    /**
     * いずれ壊されることが確定している場合true
     */
    var willBroken: Boolean = false,
) {
    enum class FieldObject {
        NONE, UNBREAKABLE_WALL, BREAKABLE_WALL, BOMB, POWER_UP_ITEM
    }

    val isPassable: Boolean
        get() = fieldObject == FieldObject.NONE || fieldObject == FieldObject.POWER_UP_ITEM
}