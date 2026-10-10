package com.example.math

data class PrimeNumberInfo(
    val number: Int,
    val isPrime: Boolean,
    val explanationBn: String,
    val factors: List<Int>
) {
    val bnNumber: String get() = BengaliNumberFormatter.toBn(number)
}

enum class PrimeRange(val rangeId: String, val titleBn: String, val startNum: Int, val endNum: Int, val expectedCount: Int) {
    RANGE_1_100("prime_1_100", "১ থেকে ১০০ এর মৌলিক সংখ্যা (২৫ টি)", 1, 100, 25),
    RANGE_101_200("prime_101_200", "১০১ থেকে ২০০ এর মৌলিক সংখ্যা (২১ টি)", 101, 200, 21)
}

object PrimeNumberProvider {
    fun isPrime(n: Int): Boolean {
        if (n <= 1) return false
        if (n <= 3) return true
        if (n % 2 == 0 || n % 3 == 0) return false
        var i = 5
        while (i * i <= n) {
            if (n % i == 0 || n % (i + 2) == 0) return false
            i += 6
        }
        return true
    }

    fun getFactors(n: Int): List<Int> {
        val factors = mutableListOf<Int>()
        for (i in 1..n) {
            if (n % i == 0) factors.add(i)
        }
        return factors
    }

    fun getPrimesInRange(start: Int, end: Int): List<Int> {
        return (start..end).filter { isPrime(it) }
    }

    fun getPrimeInfo(n: Int): PrimeNumberInfo {
        val prime = isPrime(n)
        val factors = getFactors(n)
        val bnNum = BengaliNumberFormatter.toBn(n)
        val explanation = when {
            n <= 1 -> "$bnNum মৌলিক বা যৌগিক কোনো সংখ্যাই নয়।"
            prime -> "$bnNum একটি মৌলিক সংখ্যা। এর কেবল দুটি গুণনীয়ক রয়েছে: ১ এবং $bnNum।"
            else -> {
                val factorsBn = factors.joinToString(", ") { BengaliNumberFormatter.toBn(it) }
                "$bnNum একটি যৌগিক সংখ্যা। এর গুণনীয়কগুলো হলো: $factorsBn।"
            }
        }
        return PrimeNumberInfo(
            number = n,
            isPrime = prime,
            explanationBn = explanation,
            factors = factors
        )
    }

    // Quick verification: 1-100 has 25 primes, 101-200 has 21 primes
    val primes1To100: List<Int> by lazy { getPrimesInRange(1, 100) }
    val primes101To200: List<Int> by lazy { getPrimesInRange(101, 200) }
}
