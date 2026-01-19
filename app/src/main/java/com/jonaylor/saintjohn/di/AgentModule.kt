package com.jonaylor.saintjohn.di

import com.jonaylor.saintjohn.domain.agent.AgentSkill
import com.jonaylor.saintjohn.domain.agent.SkillRegistry
import com.jonaylor.saintjohn.domain.agent.skills.LaunchAppSkill
import com.jonaylor.saintjohn.domain.agent.skills.GetWeatherSkill
import com.jonaylor.saintjohn.domain.agent.skills.ListAppsSkill
import com.jonaylor.saintjohn.domain.agent.skills.CreateNoteSkill
import com.jonaylor.saintjohn.domain.agent.skills.GetCalendarEventsSkill
import com.jonaylor.saintjohn.domain.agent.skills.WebSearchSkill
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AgentModule {

    @Provides
    @IntoSet
    fun provideLaunchAppSkill(skill: LaunchAppSkill): AgentSkill {
        return skill
    }

    @Provides
    @IntoSet
    fun provideGetWeatherSkill(skill: GetWeatherSkill): AgentSkill {
        return skill
    }

    @Provides
    @IntoSet
    fun provideListAppsSkill(skill: ListAppsSkill): AgentSkill {
        return skill
    }

    @Provides
    @IntoSet
    fun provideCreateNoteSkill(skill: CreateNoteSkill): AgentSkill {
        return skill
    }

    @Provides
    @IntoSet
    fun provideGetCalendarEventsSkill(skill: GetCalendarEventsSkill): AgentSkill {
        return skill
    }

    @Provides
    @IntoSet
    fun provideWebSearchSkill(skill: WebSearchSkill): AgentSkill {
        return skill
    }

    @Provides
    @Singleton
    fun provideSkillRegistry(skills: Set<@JvmSuppressWildcards AgentSkill>): SkillRegistry {
        return SkillRegistry(skills)
    }
}
