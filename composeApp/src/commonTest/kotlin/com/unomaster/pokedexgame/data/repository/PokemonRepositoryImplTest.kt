package com.unomaster.pokedexgame.data.repository

import com.unomaster.pokedexgame.domain.error.DomainError
import com.unomaster.pokedexgame.fake.FakePokemonRemoteSource
import com.unomaster.pokedexgame.fake.FixedRandomSource
import com.unomaster.pokedexgame.fake.RecordingAnalytics
import com.unomaster.pokedexgame.fake.pokemonDetailDto
import com.unomaster.pokedexgame.fake.pokemonPageDto
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PokemonRepositoryImplTest {

    private fun repository(
        remoteSource: FakePokemonRemoteSource = FakePokemonRemoteSource(),
        analytics: RecordingAnalytics = RecordingAnalytics(),
        answerIndex: Int = 0,
    ) = PokemonRepositoryImpl(
        remoteSource = remoteSource,
        randomSource = FixedRandomSource(answerIndex),
        analytics = analytics,
    )

    @Test
    fun buildsQuestionFromThePickedEntryAndItsDetail() = runTest {
        // FixedRandomSource(0) picks the first entry — bulbasaur — and preserves list order, so
        // every field below is an exact assertion rather than a "contains four names" hedge.
        val result = repository().getQuestion(pageUrl = null)

        val question = result.getOrNull()!!
        assertEquals("Bulbasaur", question.answerName)
        assertEquals("https://example.test/bulbasaur.png", question.artworkUrl)
        assertEquals(
            listOf("Charmander", "Squirtle", "Pikachu", "Bulbasaur"),
            question.choices,
        )
        assertEquals("https://example.test/pokemon?offset=20", question.nextPageUrl)
    }

    // The dex entry's two fields come off the same detail response as the artwork, and the type
    // names arrive as lowercase slugs, so the mapping is worth asserting rather than assuming.
    @Test
    fun carriesTheDexNumberAndTypesFromTheDetail() = runTest {
        val remoteSource = FakePokemonRemoteSource(
            detail = pokemonDetailDto(id = 25, types = listOf("electric", "flying")),
        )

        val question = repository(remoteSource = remoteSource)
            .getQuestion(pageUrl = null)
            .getOrNull()!!

        assertEquals(25, question.pokedexNumber)
        assertEquals(listOf("Electric", "Flying"), question.types)
    }

    // A payload without a types array still has to produce a playable round: the label is a
    // nice-to-have, and failing the whole load over it would show the player "couldn't connect".
    @Test
    fun missingTypes_stillBuildsARound() = runTest {
        val remoteSource = FakePokemonRemoteSource(detail = pokemonDetailDto(types = emptyList()))

        val question = repository(remoteSource = remoteSource)
            .getQuestion(pageUrl = null)
            .getOrNull()!!

        assertEquals(emptyList(), question.types)
    }

    @Test
    fun fetchesTheDetailOfThePickedEntry() = runTest {
        val remoteSource = FakePokemonRemoteSource()

        repository(remoteSource = remoteSource, answerIndex = 3).getQuestion(pageUrl = null)

        assertEquals(
            listOf("https://example.test/pokemon/pikachu"),
            remoteSource.requestedDetailUrls,
        )
    }

    @Test
    fun logsOneAnalyticsEventOnSuccess() = runTest {
        val analytics = RecordingAnalytics()

        repository(
            remoteSource = FakePokemonRemoteSource(detail = pokemonDetailDto(id = 25)),
            analytics = analytics,
        ).getQuestion(pageUrl = null)

        assertEquals(listOf("pokemon_question_loaded"), analytics.loggedEventNames)
        assertEquals("25", analytics.loggedParams.single()["pokemon_id"])
    }

    // The short-circuit is worth its own test: it is easy to write a repository that logs the
    // analytics event before binding, and no assertion on the happy path would catch it.
    @Test
    fun onDetailFailure_returnsLeft_andLogsNothing() = runTest {
        val analytics = RecordingAnalytics()

        val result = repository(
            remoteSource = FakePokemonRemoteSource(
                detailFailsWith = DomainError.NetworkUnavailable(),
            ),
            analytics = analytics,
        ).getQuestion(pageUrl = null)

        assertTrue(result.isLeft())
        assertTrue(analytics.loggedEventNames.isEmpty())
    }

    @Test
    fun onPageFailure_neverAsksForADetail() = runTest {
        val remoteSource = FakePokemonRemoteSource(pageFailsWith = DomainError.RateLimited())

        val result = repository(remoteSource = remoteSource).getQuestion(pageUrl = null)

        assertTrue(result.isLeft())
        assertTrue(remoteSource.requestedDetailUrls.isEmpty())
    }

    @Test
    fun tooFewResultsToBuildARound_isEmptyResponse() = runTest {
        val remoteSource = FakePokemonRemoteSource(
            page = pokemonPageDto(names = listOf("bulbasaur", "charmander")),
        )

        val result = repository(remoteSource = remoteSource).getQuestion(pageUrl = null)

        assertEquals(DomainError.EmptyResponse, result.leftOrNull())
    }
}
