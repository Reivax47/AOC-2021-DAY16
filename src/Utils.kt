import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = File("src", "$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5(): String = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

/**
 *
 */
fun conversionHexToBinary(chaine: String): String {

    val convert = mapOf(
        "0" to "0000",
        "1" to "0001",
        "2" to "0010",
        "3" to "0011",
        "4" to "0100",
        "5" to "0101",
        "6" to "0110",
        "7" to "0111",
        "8" to "1000",
        "9" to "1001",
        "A" to "1010",
        "B" to "1011",
        "C" to "1100",
        "D" to "1101",
        "E" to "1110",
        "F" to "1111"
    )
    var chaine_binaire = ""
    chaine.forEach {
        chaine_binaire += convert[it.toString()]
    }
    return chaine_binaire
}

class packet(val tableau: String, val depart: Int) {

    val mesPackets = mutableListOf<packet>()
    val version = tableau.substring(depart, depart + 3).toInt(2)
    val id = tableau.substring(depart + 3, depart + 6).toInt(2)
    var literal_value: Long = 0
    var fin: Int = 0

    init {

        when (this.id) {

            4 -> {
                var compteur = depart + 6
                var literal = tableau.substring(compteur, compteur + 5)
                var literal_complet = literal.substring(1)
                compteur += 5
                while (literal.substring(0, 1) != "0") {
                    literal = tableau.substring(compteur, compteur + 5)
                    literal_complet += literal.substring(1)
                    compteur += 5
                }
                this.literal_value = literal_complet.toLong(2)
                this.fin = compteur
            }
            else -> {
                var compteur = depart + 6
                val I_bit = tableau.subSequence(compteur, compteur + 1).toString().toInt(2)
                if (I_bit == 0) {

                    compteur++
                    val test = tableau.substring(compteur, compteur + 15)
                    val longueurs_subs = tableau.substring(compteur, compteur + 15).toInt(2)
                    compteur += 15
                    var depuis = compteur
                    while (depuis - compteur < longueurs_subs) {
                        this.mesPackets.add(packet(tableau, depuis))
                        depuis = this.mesPackets.last().fin
                    }
                    this.fin = depuis
                } else {

                    compteur++
                    val nb_subpackets = tableau.substring(compteur + 1, compteur + 11).toInt(2)
                    var depuis = compteur + 11
                    for (i in 1..nb_subpackets) {

                        this.mesPackets.add(packet(tableau, depuis))
                        depuis = this.mesPackets.last().fin
                    }
                    this.fin = depuis
                }
            }
        }
    }

    fun sommeVersion(): Int {
        return this.version + mesPackets.sumOf { it.sommeVersion() }
    }

    fun getValue(): Long {

        when (this.id) {

            4 -> {
                return literal_value
            }

            0 -> {
                return this.mesPackets.sumOf { it.getValue() }
            }

            1 -> {
                var produit: Long = 1
                this.mesPackets.forEach {
                    produit *= it.getValue()
                }
                return produit
            }

            2 -> {
                return this.mesPackets.minOf { it.getValue() }
            }

            3 -> {
                return this.mesPackets.maxOf { it.getValue() }
            }

            5 -> {

                return if(this.mesPackets[0].getValue() > this.mesPackets[1].getValue()) { 1L } else { 0L }
            }

            6 -> {

                return if(this.mesPackets[0].getValue() < this.mesPackets[1].getValue()) { 1L } else { 0L }
            }

            7 -> {

                return if(this.mesPackets[0].getValue() == this.mesPackets[1].getValue()) { 1L } else { 0L }
            }
        }

        return 0
    }


}
