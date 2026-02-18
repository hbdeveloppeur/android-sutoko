package fr.purpletear.sutoko.di

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.purpletear.ai_conversation.data.dao.MediaDao
import com.purpletear.ai_conversation.data.dao.StyleDao
import com.purpletear.ai_conversation.data.database.AiConversationDatabase
import com.purpletear.ai_conversation.data.messaging.ImageGenerationRequestMessageHandlerImpl
import com.purpletear.ai_conversation.data.messaging.NewCharacterMessageHandlerImpl
import com.purpletear.ai_conversation.data.parser.WebsocketMessageParserImpl
import com.purpletear.ai_conversation.data.remote.CharacterApi
import com.purpletear.ai_conversation.data.remote.ImageGenerationApi
import com.purpletear.ai_conversation.data.remote.MediaApi
import com.purpletear.ai_conversation.data.remote.MessageApi
import com.purpletear.ai_conversation.data.remote.StoryChoiceApi
import com.purpletear.ai_conversation.data.remote.StyleApi
import com.purpletear.ai_conversation.data.remote.UserConfigApi
import com.purpletear.ai_conversation.data.remote.VersionApi
import com.purpletear.ai_conversation.data.remote.adapters.MessageRoleTypeAdapter
import com.purpletear.ai_conversation.data.remote.adapters.MessageStateTypeAdapter
import com.purpletear.ai_conversation.data.remote.deserializer.AiCharacterDeserializer
import com.purpletear.ai_conversation.data.remote.deserializer.message.MessageDeserializer
import com.purpletear.ai_conversation.data.remote.websocket.WebSocketDataSourceImpl
import com.purpletear.ai_conversation.data.repository.CharacterRepositoryImpl
import com.purpletear.ai_conversation.data.repository.ConversationRepositoryImpl
import com.purpletear.ai_conversation.data.repository.FileManagerRepositoryImpl
import com.purpletear.ai_conversation.data.repository.FileRepositoryImpl
import com.purpletear.ai_conversation.data.repository.ImageGenerationRepositoryImpl
import com.purpletear.ai_conversation.data.repository.MediaRepositoryImpl
import com.purpletear.ai_conversation.data.repository.MessageQueueImpl
import com.purpletear.ai_conversation.data.repository.MessageRepositoryImpl
import com.purpletear.ai_conversation.data.repository.MicrophoneRepositoryImpl
import com.purpletear.ai_conversation.data.repository.StoryChoiceRepositoryImpl
import com.purpletear.ai_conversation.data.repository.StyleRepositoryImpl
import com.purpletear.ai_conversation.data.repository.UserConfigRepositoryImpl
import com.purpletear.ai_conversation.data.repository.VersionRepositoryImpl
import com.purpletear.ai_conversation.domain.enums.MessageRole
import com.purpletear.ai_conversation.domain.enums.MessageState
import com.purpletear.ai_conversation.domain.messaging.ImageGenerationRequestMessageHandler
import com.purpletear.ai_conversation.domain.messaging.NewCharacterMessageHandler
import com.purpletear.ai_conversation.domain.model.AiCharacter
import com.purpletear.ai_conversation.domain.model.messages.entities.Message
import com.purpletear.ai_conversation.domain.parser.WebsocketMessageParser
import com.purpletear.ai_conversation.domain.repository.CharacterRepository
import com.purpletear.ai_conversation.domain.repository.ConversationRepository
import com.purpletear.ai_conversation.domain.repository.FileManagerRepository
import com.purpletear.ai_conversation.domain.repository.FileRepository
import com.purpletear.ai_conversation.domain.repository.ImageGenerationRepository
import com.purpletear.ai_conversation.domain.repository.MediaRepository
import com.purpletear.ai_conversation.domain.repository.MessageQueue
import com.purpletear.ai_conversation.domain.repository.MessageRepository
import com.purpletear.ai_conversation.domain.repository.MicrophoneRepository
import com.purpletear.ai_conversation.domain.repository.StoryChoiceRepository
import com.purpletear.ai_conversation.domain.repository.StyleRepository
import com.purpletear.ai_conversation.domain.repository.UserConfigRepository
import com.purpletear.ai_conversation.domain.repository.VersionRepository
import com.purpletear.ai_conversation.domain.repository.WebSocketDataSource
import com.purpletear.core.image_downloader.ImageDownloader
import com.purpletear.core.image_downloader.ImageDownloaderImpl
import com.purpletear.core.permission.PermissionChecker
import com.purpletear.core.permission.PermissionCheckerImpl
import com.purpletear.core.remote.Server
import com.purpletear.sutoko.notification.repository.NotificationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AiConversationAppModule {

    @Provides
    @Singleton
    fun provideImageGenerationApi(): ImageGenerationApi {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("${Server.urlPrefix()}/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ImageGenerationApi::class.java)
    }

    @Provides
    @Singleton
    fun provideStoryChoiceApi(): StoryChoiceApi {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("${Server.urlPrefix()}/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(StoryChoiceApi::class.java)
    }


    @Provides
    @Singleton
    fun provideAiConversationDatabase(app: Application): AiConversationDatabase {
        return Room.databaseBuilder(
            app,
            AiConversationDatabase::class.java,
            "ai_conversation_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideStyleDao(db: AiConversationDatabase): StyleDao =
        db.styleDao()

    @Provides
    @Singleton
    fun provideMediaDao(db: AiConversationDatabase): MediaDao =
        db.mediaDao()

    @Provides
    @Singleton
    fun provideImageGenerationRepository(
        api: ImageGenerationApi,
    ): ImageGenerationRepository {
        return ImageGenerationRepositoryImpl(
            api = api,
        )
    }

    @Provides
    @Singleton
    fun provideStoryChoiceRepository(
        api: StoryChoiceApi,
    ): StoryChoiceRepository {
        return StoryChoiceRepositoryImpl(
            api = api,
        )
    }


    @Provides
    @Singleton
    fun provideUserConfigApi(): UserConfigApi {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("${Server.urlPrefix()}/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(UserConfigApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserConfigRepository(
        api: UserConfigApi,
    ): UserConfigRepository {
        return UserConfigRepositoryImpl(
            api = api
        )
    }

    @Provides
    @Singleton
    fun provideImageGenerationRequestMessageHandler(
        repository: ImageGenerationRepository,
        mediaRepository: MediaRepository,
    ): ImageGenerationRequestMessageHandler {
        return ImageGenerationRequestMessageHandlerImpl(
            repository = repository,
            mediaRepository = mediaRepository
        )
    }

    @Provides
    @Singleton
    fun provideNewCharacterRequestMessageHandler(
        repository: NotificationRepository,
    ): NewCharacterMessageHandler {
        return NewCharacterMessageHandlerImpl(
            notificationRepository = repository,
        )
    }

    @Provides
    @Singleton
    fun providePermissionChecker(@ApplicationContext context: Context): PermissionChecker {
        return PermissionCheckerImpl(context = context)
    }

    @Provides
    @Singleton
    fun provideImageDownloader(@ApplicationContext context: Context): ImageDownloader {
        return ImageDownloaderImpl(context)
    }

    @Provides
    @Singleton
    fun provideFileRepository(@ApplicationContext context: Context): FileRepository {
        return FileRepositoryImpl(context)
    }


    @Provides
    @Singleton
    fun provideConversationRepository(api: MessageApi): ConversationRepository {
        return ConversationRepositoryImpl(api = api)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(
        api: MessageApi,
    ): MessageRepository {
        return MessageRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideStyleApi(): StyleApi {
        val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("${Server.urlPrefix()}/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(StyleApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMediaApi(): MediaApi {
        val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("${Server.urlPrefix()}/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(MediaApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMessagesApi(): MessageApi {
        val customLogger =
            HttpLoggingInterceptor.Logger { message ->
                Log.d("RetrofitApiResponse", message)
            }

        val loggingInterceptor = HttpLoggingInterceptor(customLogger).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val customGson = GsonBuilder()
            .registerTypeAdapter(
                Message::class.java,
                MessageDeserializer()
            )
            .registerTypeAdapter(MessageState::class.java, MessageStateTypeAdapter())
            .registerTypeAdapter(MessageRole::class.java, MessageRoleTypeAdapter())
            .create()

        val build = Retrofit.Builder()
            .baseUrl("${Server.urlPrefix()}/api/")
            .addConverterFactory(GsonConverterFactory.create(customGson))
            .client(okHttpClient)
            .build()

        return build.create(MessageApi::class.java)
    }

    @Provides
    @Singleton
    fun provideStyleRepository(api: StyleApi): StyleRepository {
        return StyleRepositoryImpl(api = api)
    }

    @Provides
    @Singleton
    fun provideMessageQueue(): MessageQueue {
        return MessageQueueImpl()
    }

    @Provides
    @Singleton
    fun provideCharacterApi(): CharacterApi {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val noCacheInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val requestWithNoCache = originalRequest.newBuilder()
                .header("Cache-Control", "no-cache")
                .header("Pragma", "no-cache")
                .build()
            chain.proceed(requestWithNoCache)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(noCacheInterceptor)
            .cache(null)
            .build()


        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(AiCharacter::class.java, AiCharacterDeserializer())
            .create()

        return Retrofit.Builder()
            .baseUrl("${Server.urlPrefix()}/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
            .create(CharacterApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCharacterRepository(api: CharacterApi): CharacterRepository {
        return CharacterRepositoryImpl(api = api)
    }

    @Provides
    @Singleton
    fun provideMediaRepository(dao: MediaDao, mediaApi: MediaApi): MediaRepository {
        return MediaRepositoryImpl(dao = dao, api = mediaApi)
    }

    @Provides
    @Singleton
    fun provideWebSocketRequest(): Request {
        try {
            return Request
                .Builder()
                .url(Server.webSocket())
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    @Provides
    @Singleton
    fun provideChatMessageParser(): WebsocketMessageParser {
        return WebsocketMessageParserImpl(
            gson = Gson()
        )
    }

    @Provides
    @Singleton
    fun provideWebSocketDataSource(
        wsRequest: Request,
        chatMessageParser: WebsocketMessageParser
    ): WebSocketDataSource {
        return WebSocketDataSourceImpl(
            webSocketRequest = wsRequest,
            websocketMessageParser = chatMessageParser
        )
    }

    @Provides
    @Singleton
    fun provideAudioRecordFileManager(@ApplicationContext context: Context): FileManagerRepository {
        return FileManagerRepositoryImpl(
            context.contentResolver,
            context
        )
    }

    @Provides
    @Singleton
    fun provideAudioRecordRepository(
        @ApplicationContext context: Context,
        fileManager: FileManagerRepository
    ): MicrophoneRepository {
        return MicrophoneRepositoryImpl(
            context,
            fileManager
        )
    }

    @Provides
    @Singleton
    fun provideVersionApi(): VersionApi {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("${Server.urlPrefix()}/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(VersionApi::class.java)
    }

    @Provides
    @Singleton
    fun provideVersionRepository(
        api: VersionApi
    ): VersionRepository {
        return VersionRepositoryImpl(
            api
        )
    }

}