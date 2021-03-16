package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.Question
import blue.mild.covid.vaxx.dto.response.QuestionDtoOut
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pw.forst.tools.katlib.toUuid

/**
 * Service with access to [Question], internally it uses
 * [MemoryCacheService] as the questions are not changed very often.
 */
class QuestionService {

    private val cache by lazy {
        // we can totally wait for the first time
        MemoryCacheService(dataInit = ::getAllQuestions)
            .apply { runBlocking { initialize() } }
    }

    /**
     * Returns cached questions.
     */
    suspend fun getCachedQuestions(): List<QuestionDtoOut> = cache.value()

    /**
     * Enforces cache refresh and returns new data.
     */
    suspend fun refreshCache() = cache.refresh()

    /**
     * Get all questions from the database.
     */
    private suspend fun getAllQuestions(): List<QuestionDtoOut> = newSuspendedTransaction {
        Question.selectAll().map {
            QuestionDtoOut(
                id = it[Question.id].toUuid(),
                placeholder = it[Question.placeholder],
                cs = it[Question.cs],
                eng = it[Question.eng],
            )
        }.sortedBy { it.id } // sort it to keep it consistent
    }
}
