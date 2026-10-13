package com.example.data

import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class KhmerArtistProfile(
    val id: String,
    val englishName: String,
    val khmerName: String,
    val genreBadge: String,
    val aliases: List<String>,
    val primarySearchQueries: List<String>,
    val popularSongs: List<String> = emptyList()
) {
    val displayName: String = "$englishName ($khmerName)"
    val englishLower: String = englishName.lowercase(Locale.ROOT)
    val displayLower: String = displayName.lowercase(Locale.ROOT)
    val aliasesLower: List<String> = aliases.map { it.lowercase(Locale.ROOT) }
    val popularSongsLower: List<String> = popularSongs.map { it.lowercase(Locale.ROOT) }
    val prefixStripRegex: Regex = run {
        val escaped = (listOf(displayName, englishName, khmerName) + aliases)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sortedByDescending { it.length }
            .joinToString("|") { Regex.escape(it) }
        Regex("^\\s*(?:$escaped)\\s*[-–—:|]\\s*", RegexOption.IGNORE_CASE)
    }
}

object KhmerArtistDirectory {
    private val WHITESPACE_REGEX = Regex("\\s+")
    private val matchingArtistsCache = ConcurrentHashMap<String, List<KhmerArtistProfile>>()


    val featuredArtists: List<KhmerArtistProfile> = listOf(
        KhmerArtistProfile(
            id = "khmer_vannda",
            englishName = "VannDa",
            khmerName = "វណ្ណដា",
            genreBadge = "Khmer Hip-Hop & Rap",
            aliases = listOf("vannda", "vanda", "mann vannda", "វណ្ណដា", "ម៉ាន់ វណ្ណដា", "baramey"),
            primarySearchQueries = listOf("VannDa", "វណ្ណដា"),
            popularSongs = listOf("Time To Rise", "J+O", "Solo", "Mama", "Bong", "Hot Boy", "Hikari", "Parenthesis", "Catch Me If You Can", "Queen Bee")
        ),
        KhmerArtistProfile(
            id = "khmer_tena",
            englishName = "Tena",
            khmerName = "ថេណា",
            genreBadge = "Khmer R&B & Indie Pop",
            aliases = listOf("tena", "tena khimphun", "tena khmer", "ថេណា"),
            primarySearchQueries = listOf("Tena Khmer", "ថេណា"),
            popularSongs = listOf("Khmeanalan", "Love Is Singular", "First Lady", "Good Boy", "Pheapika", "Kmean Avey Lerng", "1000 Years")
        ),
        KhmerArtistProfile(
            id = "khmer_suly_pheng",
            englishName = "Suly Pheng",
            khmerName = "ស៊ូលីផេង",
            genreBadge = "Khmer Acoustic & Soul",
            aliases = listOf("suly pheng", "sulypheng", "suly", "ស៊ូលីផេង", "ស៊ូលី ផេង"),
            primarySearchQueries = listOf("Suly Pheng", "ស៊ូលីផេង"),
            popularSongs = listOf("Phka Proheus", "10K", "Yesterday", "Pdey Laor", "Srolanh Oun", "Kompul Sneh")
        ),
        KhmerArtistProfile(
            id = "khmer_g_devith",
            englishName = "G-Devith",
            khmerName = "ជី ដេវីត",
            genreBadge = "Khmer Hip-Hop & Rap",
            aliases = listOf("g-devith", "g devith", "gdevith", "devith", "dit way", "ជី ដេវីត", "ជីដេវីត", "ដេវីត"),
            primarySearchQueries = listOf("G-Devith", "ជី ដេវីត"),
            popularSongs = listOf("Body", "Bodyguard", "No Smoke", "Rambo", "Life", "Oknha", "Bong Pouv", "350", "F.A.K.E", "Reincarnation")
        ),
        KhmerArtistProfile(
            id = "khmer_preap_sovath",
            englishName = "Preap Sovath",
            khmerName = "ព្រាប សុវត្ថិ",
            genreBadge = "Khmer Pop Legend",
            aliases = listOf("preap sovath", "preapsovath", "preap", "sovath", "ព្រាប សុវត្ថិ", "ព្រាបសុវត្ថិ"),
            primarySearchQueries = listOf("Preap Sovath", "ព្រាប សុវត្ថិ"),
            popularSongs = listOf("U Better Not Come Home", "Srolanh Oun", "Peakmi", "Khmer")
        ),
        KhmerArtistProfile(
            id = "khmer_sinn_sisamouth",
            englishName = "Sinn Sisamouth",
            khmerName = "ស៊ីន ស៊ីសាមុត",
            genreBadge = "Khmer Golden Era",
            aliases = listOf("sinn sisamouth", "sin sisamouth", "sisamouth", "ស៊ីន ស៊ីសាមុត", "ស៊ិន ស៊ីសាមុត"),
            primarySearchQueries = listOf("Sinn Sisamouth", "ស៊ីន ស៊ីសាមុត"),
            popularSongs = listOf("Champa Battambang", "Violon Sneha", "Koupreah", "Phnom Penh")
        ),
        KhmerArtistProfile(
            id = "khmer_khem",
            englishName = "Khem",
            khmerName = "ខេម",
            genreBadge = "Khmer Modern Ballad",
            aliases = listOf("khem", "khem khmer", "khem town", "ខេម"),
            primarySearchQueries = listOf("Khem Khmer", "ខេម")
        ),
        KhmerArtistProfile(
            id = "khmer_meas_soksophea",
            englishName = "Meas Soksophea",
            khmerName = "មាស សុខសោភា",
            genreBadge = "Khmer Pop Diva",
            aliases = listOf("meas soksophea", "soksophea", "មាស សុខសោភា"),
            primarySearchQueries = listOf("Meas Soksophea", "មាស សុខសោភា")
        ),
        KhmerArtistProfile(
            id = "khmer_aok_sokunkanha",
            englishName = "Aok Sokunkanha",
            khmerName = "ឱក សុគន្ធកញ្ញា",
            genreBadge = "Khmer Pop & Ballad",
            aliases = listOf("aok sokunkanha", "ouk sokunkanha", "sokunkanha", "ឱក សុគន្ធកញ្ញា"),
            primarySearchQueries = listOf("Aok Sokunkanha", "ឱក សុគន្ធកញ្ញា")
        ),
        KhmerArtistProfile(
            id = "khmer_chhay_virakyuth",
            englishName = "Chhay Virakyuth",
            khmerName = "ឆាយ វីរៈយុទ្ធ",
            genreBadge = "Khmer Ballad",
            aliases = listOf("chhay virakyuth", "chhay virakyut", "virakyuth", "ឆាយ វីរៈយុទ្ធ"),
            primarySearchQueries = listOf("Chhay Virakyuth", "ឆាយ វីរៈយុទ្ធ")
        ),
        KhmerArtistProfile(
            id = "khmer_khemarak_sereymon",
            englishName = "Khemarak Sereymon",
            khmerName = "ខេមរៈ សិរីមន្ត",
            genreBadge = "Khmer Pop & Dance",
            aliases = listOf("khemarak sereymon", "sereymon", "ខេមរៈ សិរីមន្ត"),
            primarySearchQueries = listOf("Khemarak Sereymon", "ខេមរៈ សិរីមន្ត")
        ),
        KhmerArtistProfile(
            id = "khmer_kmeng_khmer",
            englishName = "Kmeng Khmer",
            khmerName = "ក្មេងខ្មែរ",
            genreBadge = "Khmer Pop Duo",
            aliases = listOf("kmeng khmer", "kmengkhmer", "khmeng khmer", "ក្មេងខ្មែរ", "ពន្លឺ", "សុជាតិ"),
            primarySearchQueries = listOf("Kmeng Khmer", "ក្មេងខ្មែរ"),
            popularSongs = listOf("Bong Nirk Oun", "Kmean Oun", "Chnam Mun", "24/7")
        ),
        KhmerArtistProfile(
            id = "khmer_noly_record",
            englishName = "Noly Record",
            khmerName = "ណូលី",
            genreBadge = "Khmer Indie & R&B",
            aliases = listOf("noly", "noly record", "noly time", "ណូលី"),
            primarySearchQueries = listOf("Noly Record", "ណូលី")
        ),
        KhmerArtistProfile(
            id = "khmer_vuthea",
            englishName = "Vuthea",
            khmerName = "វុទ្ធា",
            genreBadge = "Khmer Indie & Soul",
            aliases = listOf("vuthea", "វុទ្ធា"),
            primarySearchQueries = listOf("Vuthea Khmer", "វុទ្ធា")
        ),
        KhmerArtistProfile(
            id = "khmer_tep_boprek",
            englishName = "Tep Boprek",
            khmerName = "ទេព បូព្រឹក្ស",
            genreBadge = "Khmer Pop & Dance Queen",
            aliases = listOf("tep boprek", "boprek", "បូព្រឹក្ស", "ទេព បូព្រឹក្ស"),
            primarySearchQueries = listOf("Tep Boprek", "ទេព បូព្រឹក្ស")
        ),
        KhmerArtistProfile(
            id = "khmer_pich_sophea",
            englishName = "Pich Sophea",
            khmerName = "ពេជ្រ សោភា",
            genreBadge = "Khmer Pop",
            aliases = listOf("pich sophea", "ពេជ្រ សោភា"),
            primarySearchQueries = listOf("Pich Sophea", "ពេជ្រ សោភា")
        ),
        KhmerArtistProfile(
            id = "khmer_ton_chanseyma",
            englishName = "Ton Chanseyma",
            khmerName = "តន់ ចន្ទសីម៉ា",
            genreBadge = "Khmer Modern Pop",
            aliases = listOf("ton chanseyma", "chanseyma", "តន់ ចន្ទសីម៉ា"),
            primarySearchQueries = listOf("Ton Chanseyma", "តន់ ចន្ទសីម៉ា")
        ),
        KhmerArtistProfile(
            id = "khmer_sok_pisey",
            englishName = "Sok Pisey",
            khmerName = "សុខ ពិសី",
            genreBadge = "Khmer Sweet Ballad",
            aliases = listOf("sok pisey", "សុខ ពិសី"),
            primarySearchQueries = listOf("Sok Pisey", "សុខ ពិសី")
        ),
        KhmerArtistProfile(
            id = "khmer_ros_sereysothea",
            englishName = "Ros Sereysothea",
            khmerName = "រស់ សេរីសុទ្ធា",
            genreBadge = "Khmer Golden Classic",
            aliases = listOf("ros sereysothea", "ros serey sothea", "sereysothea", "រស់ សេរីសុទ្ធា"),
            primarySearchQueries = listOf("Ros Sereysothea", "រស់ សេរីសុទ្ធា")
        ),
        KhmerArtistProfile(
            id = "khmer_ycn_rakhie",
            englishName = "YCN Rakhie",
            khmerName = "រ៉ាឃី",
            genreBadge = "Khmer Hip-Hop & R&B",
            aliases = listOf("ycn rakhie", "rakhie", "រ៉ាឃី"),
            primarySearchQueries = listOf("YCN Rakhie", "រ៉ាឃី")
        ),
        KhmerArtistProfile(
            id = "khmer_kanhchna_chet",
            englishName = "Kanhchna Chet",
            khmerName = "កញ្ចនា ឆេត",
            genreBadge = "Khmer Indie Vocal",
            aliases = listOf("kanhchna chet", "kanhchna", "កញ្ចនា ឆេត"),
            primarySearchQueries = listOf("Kanhchna Chet", "កញ្ចនា ឆេត")
        ),
        KhmerArtistProfile(
            id = "khmer_chen",
            englishName = "Chen",
            khmerName = "ចេន",
            genreBadge = "Khmer Indie Ballad",
            aliases = listOf("chen khmer", "chen", "ចេន"),
            primarySearchQueries = listOf("Chen Khmer", "ចេន")
        ),
        KhmerArtistProfile(
            id = "khmer_heng_pitu",
            englishName = "Heng Pitu",
            khmerName = "ហេង ពិទូ",
            genreBadge = "Khmer Acoustic Pop",
            aliases = listOf("heng pitu", "pitu", "ហេង ពិទូ"),
            primarySearchQueries = listOf("Heng Pitu", "ហេង ពិទូ")
        ),
        KhmerArtistProfile(
            id = "khmer_p_sand",
            englishName = "P-Sand",
            khmerName = "ភីសាន់",
            genreBadge = "Khmer R&B",
            aliases = listOf("p-sand", "psand", "p sand", "ភីសាន់"),
            primarySearchQueries = listOf("P-Sand Khmer", "ភីសាន់")
        ),
        KhmerArtistProfile(
            id = "khmer_sai",
            englishName = "SAI",
            khmerName = "សាយ",
            genreBadge = "Khmer Pop & Rock",
            aliases = listOf("sai khmer", "សាយ"),
            primarySearchQueries = listOf("SAI Khmer", "សាយ")
        ),
        KhmerArtistProfile(
            id = "khmer_eno",
            englishName = "ENO",
            khmerName = "អ៊ីណូ",
            genreBadge = "Khmer Pop",
            aliases = listOf("eno khmer", "eno", "អ៊ីណូ"),
            primarySearchQueries = listOf("ENO Khmer", "អ៊ីណូ")
        ),
        KhmerArtistProfile(
            id = "khmer_nico",
            englishName = "Nico",
            khmerName = "នីកូ",
            genreBadge = "Khmer Pop",
            aliases = listOf("nico khmer", "នីកូ"),
            primarySearchQueries = listOf("Nico Khmer", "នីកូ")
        ),
        KhmerArtistProfile(
            id = "khmer_keo_sarath",
            englishName = "Keo Sarath",
            khmerName = "កែវ សារ៉ាត់",
            genreBadge = "Khmer Classic Legend",
            aliases = listOf("keo sarath", "កែវ សារ៉ាត់"),
            primarySearchQueries = listOf("Keo Sarath", "កែវ សារ៉ាត់")
        ),
        KhmerArtistProfile(
            id = "khmer_pen_ran",
            englishName = "Pen Ran",
            khmerName = "ប៉ែន រ៉ន",
            genreBadge = "Khmer Golden Era",
            aliases = listOf("pen ran", "pan ron", "ប៉ែន រ៉ន"),
            primarySearchQueries = listOf("Pen Ran", "ប៉ែន រ៉ន")
        ),
        KhmerArtistProfile(
            id = "khmer_swsb",
            englishName = "Smallworld Smallband",
            khmerName = "ក្រុមតូច",
            genreBadge = "Khmer Indie Band",
            aliases = listOf("smallworld smallband", "swsb", "ក្រុមតូច"),
            primarySearchQueries = listOf("Smallworld Smallband", "ក្រុមតូច")
        ),
        KhmerArtistProfile(
            id = "khmer_eva",
            englishName = "Eva",
            khmerName = "អ៊ីវ៉ា",
            genreBadge = "Khmer Emotional Ballad",
            aliases = listOf("eva khmer", "អ៊ីវ៉ា", "ឃីន ស្រីនាថ"),
            primarySearchQueries = listOf("Eva Khmer", "អ៊ីវ៉ា")
        ),
        KhmerArtistProfile(
            id = "khmer_rabee",
            englishName = "Rabee",
            khmerName = "រ៉ាប៊ី",
            genreBadge = "Khmer Dance Pop",
            aliases = listOf("rabee", "រ៉ាប៊ី", "rabee khmer"),
            primarySearchQueries = listOf("Rabee Khmer", "រ៉ាប៊ី")
        )
    )

    private val genericKhmerSearchTerms = setOf(
        "khmer", "khmer artist", "khmer artists", "khmer song", "khmer songs",
        "khmer music", "cambodia", "cambodian", "cambodian music", "cambodian artist",
        "បទខ្មែរ", "ចម្រៀងខ្មែរ", "តារាចម្រៀងខ្មែរ", "ភ្លេងខ្មែរ", "បទថ្មី", "ខ្មែរ",
        "បទល្បី", "បទល្បីៗ", "បទចម្រៀង", "ចម្រៀងថ្មី", "មិនកំណត់"
    )

    private val khmerKeywordExpansions: Map<String, List<String>> = mapOf(
        "បទសេដ" to listOf("បទសេដ", "Khmer Sad Song", "Tena Suly Pheng"),
        "សេដ" to listOf("បទសេដ", "Khmer Sad Song"),
        "ខូចចិត្ត" to listOf("បទខូចចិត្ត", "Khmer Heartbreak Song", "Suly Pheng Tena"),
        "បទញាក់" to listOf("បទញាក់", "Khmer Remix Dance", "Khmer Club Remix"),
        "ញាក់" to listOf("បទញាក់", "Khmer Remix"),
        "រាំវង់" to listOf("រាំវង់", "Khmer Romvong", "Preap Sovath Romvong"),
        "កន្ទ្រឹម" to listOf("កន្ទ្រឹម", "Khmer Kantrem", "Khemarak Sereymon"),
        "ចូលឆ្នាំ" to listOf("បទចូលឆ្នាំខ្មែរ", "Khmer New Year Song", "រាំវង់ចូលឆ្នាំ"),
        "បទបុរាណ" to listOf("បទបុរាណខ្មែរ", "Sinn Sisamouth", "Ros Sereysothea"),
        "មរតកដើម" to listOf("ចម្រៀងមរតកដើម", "Sinn Sisamouth", "ស៊ីន ស៊ីសាមុត"),
        "ស្នេហា" to listOf("ស្នេហា", "Khmer Love Song", "Suly Pheng VannDa"),
        "នឹក" to listOf("នឹក", "Bong Nirk Oun", "Khmer Miss You Song"),
        "អនុស្សាវរីយ៍" to listOf("អនុស្សាវរីយ៍", "Khmer Memory Song", "Khem Khmer"),
        "ភ្លេងការ" to listOf("ភ្លេងការខ្មែរ", "Khmer Traditional Wedding Music")
    )

    fun containsKhmerScript(text: String): Boolean {
        for (ch in text) {
            if (ch.code in 0x1780..0x17FF || ch.code in 0x19E0..0x19FF) return true
        }
        return false
    }

    fun isGenericKhmerQuery(query: String): Boolean {
        val norm = query.trim().lowercase(Locale.ROOT)
        return norm in genericKhmerSearchTerms
    }

    fun isKhmerRelatedQuery(query: String): Boolean {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return false
        if (containsKhmerScript(trimmed)) return true
        val lower = trimmed.lowercase(Locale.ROOT)
        if (lower in genericKhmerSearchTerms || lower.contains("khmer") || lower.contains("cambodia")) {
            return true
        }
        return findMatchingArtists(trimmed).isNotEmpty()
    }

    fun findMatchingArtists(query: String): List<KhmerArtistProfile> {
        val trimmed = query.trim()
        if (trimmed.isEmpty() || isGenericKhmerQuery(trimmed)) {
            return featuredArtists
        }
        val lower = trimmed.lowercase(Locale.ROOT)
        matchingArtistsCache[lower]?.let { return it }

        val computed = featuredArtists.filter { profile ->
            profile.englishLower.contains(lower) ||
                (profile.englishLower.length >= 3 && lower.contains(profile.englishLower)) ||
                profile.khmerName.contains(trimmed) ||
                trimmed.contains(profile.khmerName) ||
                profile.displayLower.contains(lower) ||
                profile.aliasesLower.any { aliasLower ->
                    aliasLower == lower ||
                        (lower.length >= 3 && aliasLower.contains(lower)) ||
                        (aliasLower.length >= 4 && lower.contains(aliasLower))
                } ||
                profile.popularSongsLower.any { songLower ->
                    songLower == lower || (lower.length >= 6 && songLower.length >= 6 && (songLower.contains(lower) || lower.contains(songLower)))
                }
        }
        if (matchingArtistsCache.size > 256) {
            matchingArtistsCache.clear()
        }
        matchingArtistsCache[lower] = computed
        return computed
    }

    fun expandSearchQueries(rawQuery: String): List<String> {
        val trimmed = rawQuery.trim()
        if (trimmed.isEmpty()) return emptyList()

        // Check keyword expansion dictionary first
        for ((k, expansions) in khmerKeywordExpansions) {
            if (trimmed.equals(k, ignoreCase = true) || trimmed.contains(k)) {
                return (listOf(trimmed) + expansions).distinct()
            }
        }

        if (isGenericKhmerQuery(trimmed)) {
            return listOf(
                trimmed,
                "VannDa",
                "Tena Khmer",
                "Suly Pheng",
                "Preap Sovath",
                "Sinn Sisamouth",
                "Khem Khmer"
            ).distinct()
        }

        val matchedProfiles = findMatchingArtists(trimmed)
        if (matchedProfiles.isEmpty()) {
            return if (containsKhmerScript(trimmed)) {
                listOf(trimmed, "$trimmed Khmer")
            } else {
                listOf(trimmed)
            }
        }

        val expanded = linkedSetOf<String>()
        // ALWAYS put the user's exact query first so specific songs are never buried
        expanded.add(trimmed)

        val topProfile = matchedProfiles.first()
        val lower = trimmed.lowercase(Locale.ROOT)
        val isExactArtistOnly = topProfile.aliasesLower.any { it == lower } ||
            topProfile.englishLower == lower ||
            topProfile.khmerName == trimmed ||
            topProfile.displayLower == lower

        if (!isExactArtistOnly) {
            if (!lower.contains(topProfile.englishLower) &&
                !trimmed.contains(topProfile.khmerName)
            ) {
                expanded.add("${topProfile.englishName} $trimmed")
            }
        }

        expanded.addAll(topProfile.primarySearchQueries)

        if (isExactArtistOnly && matchedProfiles.size > 1) {
            expanded.addAll(matchedProfiles[1].primarySearchQueries.take(1))
        }

        return expanded.toList()
    }

    fun detectProfileInText(vararg texts: String?): KhmerArtistProfile? {
        val combined = texts.filterNotNull().joinToString(" ").trim()
        if (combined.isEmpty()) return null
        val lower = combined.lowercase(Locale.ROOT)

        for (profile in featuredArtists) {
            if (combined.contains(profile.khmerName)) return profile
            if (containsWholeTokenOrPhrase(lower, profile.englishLower)) return profile
            for (aliasLower in profile.aliasesLower) {
                if (containsKhmerScript(aliasLower)) {
                    if (combined.contains(aliasLower)) return profile
                } else if (aliasLower.length >= 4 && containsWholeTokenOrPhrase(lower, aliasLower)) {
                    return profile
                }
            }
        }
        return null
    }

    private fun containsWholeTokenOrPhrase(haystackLower: String, needleLower: String): Boolean {
        val idx = haystackLower.indexOf(needleLower)
        if (idx < 0) return false
        val beforeOk = idx == 0 || !haystackLower[idx - 1].isLetterOrDigit()
        val endIdx = idx + needleLower.length
        val afterOk = endIdx >= haystackLower.length || !haystackLower[endIdx].isLetterOrDigit()
        return beforeOk && afterOk
    }

    /**
     * Normalizes SoundCloud/online track artist and title so Khmer artists are consistently
     * attributed under their canonical bilingual name `English (ខ្មែរ)` instead of uploader handles
     * or inverted `Song - Artist` titles.
     */
    fun normalizeTrackArtistAndTitle(
        rawTitle: String,
        uploaderUsername: String,
        publisherArtist: String? = null,
        queryHint: String = ""
    ): Pair<String, String> {
        val cleanUploader = uploaderUsername.trim().ifBlank { "Global Artist" }
        val cleanPubArtist = publisherArtist?.trim().orEmpty()

        val splitSeparators = listOf(" - ", " – ", " — ", " | ", " // ")
        var leftPart = ""
        var rightPart = ""
        for (sep in splitSeparators) {
            if (rawTitle.contains(sep)) {
                leftPart = rawTitle.substringBefore(sep).trim()
                rightPart = rawTitle.substringAfter(sep).trim()
                break
            }
        }

        // Check if track text or query strongly identifies a known Khmer artist
        val detectedInTrack = detectProfileInText(rawTitle, cleanPubArtist, cleanUploader)
        val queryMatchedProfiles = if (queryHint.isNotBlank() && !isGenericKhmerQuery(queryHint)) {
            findMatchingArtists(queryHint)
        } else {
            emptyList()
        }
        val singleQueryProfile = queryMatchedProfiles.singleOrNull()
        val effectiveProfile = detectedInTrack ?: singleQueryProfile

        if (effectiveProfile != null) {
            val canonicalArtist = effectiveProfile.displayName
            val cleanedTitle = when {
                leftPart.isNotEmpty() && rightPart.isNotEmpty() -> {
                    val leftHasArtist = detectProfileInText(leftPart)?.id == effectiveProfile.id
                    val rightHasArtist = detectProfileInText(rightPart)?.id == effectiveProfile.id
                    when {
                        leftHasArtist && !rightHasArtist -> rightPart
                        rightHasArtist && !leftHasArtist -> leftPart
                        else -> rightPart.ifBlank { leftPart }
                    }
                }
                else -> stripArtistPrefixFromTitle(rawTitle, effectiveProfile)
            }
            return Pair(canonicalArtist, cleanedTitle.ifBlank { rawTitle })
        }

        // General track artist/title extraction when not in fixed profile list
        if (leftPart.isNotEmpty() && rightPart.isNotEmpty()) {
            val qLower = queryHint.trim().lowercase(Locale.ROOT)
            val rightMatchesQuery = qLower.length >= 2 && rightPart.lowercase(Locale.ROOT).contains(qLower) &&
                !leftPart.lowercase(Locale.ROOT).contains(qLower)
            return if (rightMatchesQuery) {
                Pair(rightPart, leftPart)
            } else {
                Pair(leftPart, rightPart)
            }
        }

        val fallbackArtist = cleanPubArtist.ifBlank { cleanUploader }
        return Pair(fallbackArtist, rawTitle)
    }

    private fun stripArtistPrefixFromTitle(rawTitle: String, profile: KhmerArtistProfile): String {
        val result = rawTitle.replace(profile.prefixStripRegex, "").trim()
        return result.ifBlank { rawTitle }
    }

    fun matchesKhmerArtistCatalog(item: MediaItemEntity): Boolean {
        if (item.category.contains("Khmer", ignoreCase = true)) return true
        if (item.artist.contains("(") && containsKhmerScript(item.artist)) return true
        if (containsKhmerScript(item.title) || containsKhmerScript(item.artist)) return true
        return detectProfileInText(item.artist, item.title) != null
    }

    fun isWorldTrendingTrack(item: MediaItemEntity): Boolean {
        return item.category.contains("World", ignoreCase = true) ||
            item.category.contains("Global", ignoreCase = true) ||
            item.album.contains("Chart", ignoreCase = true) ||
            item.popularityScore >= 100_000L
    }

    fun matchesMediaItem(item: MediaItemEntity, query: String): Boolean {
        val q = query.trim()
        if (q.isEmpty()) return true
        if (item.title.contains(q, ignoreCase = true) ||
            item.artist.contains(q, ignoreCase = true) ||
            item.album.contains(q, ignoreCase = true) ||
            item.category.contains(q, ignoreCase = true)
        ) {
            return true
        }
        val combinedHaystack = "${item.title} ${item.artist} ${item.album} ${item.category}".lowercase(Locale.ROOT)
        val tokens = q.lowercase(Locale.ROOT)
            .split(WHITESPACE_REGEX)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (tokens.size >= 2 && (tokens.all { combinedHaystack.contains(it) } || tokens.count { it.length >= 3 && combinedHaystack.contains(it) } >= 1)) {
            return true
        }
        if (isGenericKhmerQuery(q)) {
            return matchesKhmerArtistCatalog(item)
        }
        for ((k, expansions) in khmerKeywordExpansions) {
            if (q.contains(k, ignoreCase = true)) {
                if (expansions.any { exp -> combinedHaystack.contains(exp.lowercase(Locale.ROOT)) }) {
                    return true
                }
            }
        }
        val matchedProfiles = findMatchingArtists(q)
        for (profile in matchedProfiles) {
            if (item.artist.contains(profile.englishName, ignoreCase = true) ||
                item.artist.contains(profile.khmerName) ||
                item.title.contains(profile.englishName, ignoreCase = true) ||
                item.title.contains(profile.khmerName)
            ) {
                return true
            }
        }
        return false
    }

    /**
     * Returns the exact real worldwide listener count (YouTube views / SoundCloud playback_count
     * across all users in the world). Never invents fake numbers or boosts by 1 user's plays.
     */
    fun computePopularityScore(
        rawPlays: Long,
        likesCount: Long = 0L,
        repostsCount: Long = 0L,
        title: String,
        artist: String,
        queryHint: String = "",
        rankIndex: Int = 0
    ): Long {
        return rawPlays.coerceAtLeast(0L)
    }

    private val derivativeModifiers = listOf(
        "slowed", "sped up", "nightcore", "karaoke", "instrumental",
        "ringtone", "8d audio", "cover", "reverb"
    )

    private fun isDerivativeVersion(title: String, queryHint: String = ""): Boolean {
        val titleLower = title.lowercase(Locale.ROOT)
        val queryLower = queryHint.trim().lowercase(Locale.ROOT)
        return derivativeModifiers.any { mod ->
            titleLower.contains(mod) && !queryLower.contains(mod)
        }
    }

    /**
     * Sorts media items by real worldwide popularityScore (all users in the world),
     * never using 1 user's local playCount.
     */
    fun sortPopularSongsFirst(
        items: List<MediaItemEntity>,
        queryHint: String = "",
        prioritizeSongsBeforeVideos: Boolean = true
    ): List<MediaItemEntity> {
        if (items.size <= 1) return items
        val q = queryHint.trim()
        val checkQuery = q.isNotEmpty() && !isGenericKhmerQuery(q)
        val matchFlags = if (checkQuery) {
            items.associate { it.id to matchesMediaItem(it, q) }
        } else emptyMap()
        val derivativeFlags = items.associate { it.id to isDerivativeVersion(it.title, q) }

        return items.sortedWith(
            Comparator { a, b ->
                if (prioritizeSongsBeforeVideos && a.isVideo != b.isVideo) {
                    return@Comparator if (!a.isVideo) -1 else 1
                }
                if (checkQuery) {
                    val aMatches = matchFlags[a.id] == true
                    val bMatches = matchFlags[b.id] == true
                    if (aMatches != bMatches) {
                        return@Comparator if (aMatches) -1 else 1
                    }
                }
                val aDeriv = derivativeFlags[a.id] == true
                val bDeriv = derivativeFlags[b.id] == true
                if (aDeriv != bDeriv) {
                    return@Comparator if (!aDeriv) -1 else 1
                }

                val popCmp = b.popularityScore.compareTo(a.popularityScore)
                if (popCmp != 0) return@Comparator popCmp

                a.title.compareTo(b.title, ignoreCase = true)
            }
        )
    }

    private val OLD_YEAR_IN_TEXT_REGEX = Regex("\\b(19[6-9]\\d|20[01]\\d|202[0-5])\\b")

    /**
     * Strictly verifies that a track belongs to 2026 / Today and is NOT from an older year (1960–2025).
     */
    fun isEligibleForTrending2026OrToday(item: MediaItemEntity): Boolean {
        if (item.releaseYear in 1..2025) return false
        if (OLD_YEAR_IN_TEXT_REGEX.containsMatchIn(item.title)) return false
        if (OLD_YEAR_IN_TEXT_REGEX.containsMatchIn(item.album)) return false
        return true
    }

    /**
     * Returns the real worldwide listener play/view count across all users in the world.
     * Single-user local playCount is strictly excluded.
     */
    fun computeTrendingNewMomentum(
        item: MediaItemEntity,
        filter: TrendingFeedFilter = TrendingFeedFilter.TRENDING_TODAY
    ): Long {
        return item.popularityScore.coerceAtLeast(0L)
    }

    /**
     * Sorts trending tracks strictly by real worldwide listener counts (all users in the world),
     * keeping ONLY Trending Today and Trending 2026 tracks (never older years).
     */
    fun sortTrendingNewFirst(
        items: List<MediaItemEntity>,
        filter: TrendingFeedFilter = TrendingFeedFilter.TRENDING_TODAY
    ): List<MediaItemEntity> {
        if (items.isEmpty()) return emptyList()

        // 1. Strictly filter out any track from older years (< 2026)
        val only2026AndToday = items.filter { isEligibleForTrending2026OrToday(it) }
        val yearScopedPool = only2026AndToday.ifEmpty { items.filter { it.releaseYear >= 2026 } }

        // 2. Exclude 1-user local tracks that have 0 global plays whenever real worldwide tracks are present
        val globalItems = yearScopedPool.filter { it.popularityScore > 0L }.ifEmpty { yearScopedPool }

        val derivativeFlags = globalItems.associate { it.id to isDerivativeVersion(it.title) }

        return globalItems.sortedWith(
            Comparator { a, b ->
                val aDeriv = derivativeFlags[a.id] == true
                val bDeriv = derivativeFlags[b.id] == true
                if (aDeriv != bDeriv) {
                    return@Comparator if (!aDeriv) -1 else 1
                }
                if (filter == TrendingFeedFilter.TRENDING_TODAY) {
                    val aToday = a.category.contains("Today", ignoreCase = true) || a.licenseBadge.contains("Today", ignoreCase = true)
                    val bToday = b.category.contains("Today", ignoreCase = true) || b.licenseBadge.contains("Today", ignoreCase = true)
                    if (aToday != bToday) {
                        return@Comparator if (aToday) -1 else 1
                    }
                }
                val popCmp = b.popularityScore.compareTo(a.popularityScore)
                if (popCmp != 0) return@Comparator popCmp
                a.title.compareTo(b.title, ignoreCase = true)
            }
        )
    }

    fun trendingBadgeLabel(item: MediaItemEntity, rankIndex: Int): String {
        return if (item.popularityScore > 0L) {
            "🌍 #${rankIndex + 1} · ${item.formattedPopularity}"
        } else {
            "🌍 #${rankIndex + 1} · 2026 CHART"
        }
    }
}

enum class TrendingFeedFilter(
    val id: String,
    val label: String,
    val khmerLabel: String,
    val regionCode: String,
    val appleRssGenreId: Int?,
    val discoveryQuery: String
) {
    TRENDING_TODAY(
        "TRENDING_TODAY",
        "Trending Today",
        "កំពុងពេញនិយមថ្ងៃនេះ",
        "US",
        null,
        "Trending Music Today 2026 New Song"
    ),
    TRENDING_2026(
        "TRENDING_2026",
        "Trending 2026",
        "កំពុងពេញនិយម 2026",
        "US",
        null,
        "Top Hits 2026 Trending Songs 2026 បទថ្មី 2026"
    );

    companion object {
        val ALL_TRENDING: TrendingFeedFilter get() = TRENDING_TODAY
        val GLOBAL: TrendingFeedFilter get() = TRENDING_TODAY
        val CAMBODIA: TrendingFeedFilter get() = TRENDING_TODAY
        val POP: TrendingFeedFilter get() = TRENDING_TODAY
        val HIP_HOP: TrendingFeedFilter get() = TRENDING_2026
        val EDM: TrendingFeedFilter get() = TRENDING_2026
        val ROCK: TrendingFeedFilter get() = TRENDING_2026
        val KPOP: TrendingFeedFilter get() = TRENDING_2026
        val KHMER: TrendingFeedFilter get() = TRENDING_2026
        val RNB: TrendingFeedFilter get() = TRENDING_2026
        val KHMER_NEW: TrendingFeedFilter get() = TRENDING_2026
        val VIRAL_HITS: TrendingFeedFilter get() = TRENDING_TODAY
        val NEW_RELEASES: TrendingFeedFilter get() = TRENDING_2026
    }
}



