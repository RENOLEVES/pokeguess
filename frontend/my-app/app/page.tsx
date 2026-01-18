"use client";

import React, { useState, useEffect } from 'react';
import { Sparkles, HelpCircle, Zap } from 'lucide-react';

const PokemonGuessGame = () => {
    const [pokemon, setPokemon] = useState({id: null, imageUrl: ''});
    const [userGuess, setUserGuess] = useState('');
    const [score, setScore] = useState(0);
    const [attempts, setAttempts] = useState(0);
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);
    const [gameState, setGameState] = useState('playing');
    const [aiHint, setAiHint] = useState('');
    const [loadingAiHint, setLoadingAiHint] = useState(false);
    const [language, setLanguage] = useState<'en' | "zh">('en');

    const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8888/api';

    // run localhost
    // const API_BASE = 'http://localhost:8888/api'

    const t = {
        en: {
            title: "Who's That Pokémon?",
            score: "Score",
            attempts: "Attempts",
            inputPlaceholder: "Enter Pokemon name...",
            submit: "Submit Guess",
            checking: "Checking...",
            skip: "Skip",
            aiAssistant: "AI Assistant",
            getAiHint: "Get AI Hint",
            gettingHint: "Getting AI Hint...",
            aiPowered: "AI-powered hint using Ollama",
            gameStats: "Game Stats",
            successRate: "Success Rate",
            hintsUsed: "Hints Used",
            aiHints: "AI Hints",
            loading: "Loading Pokemon...",
            correct: "Correct! It was",
            wrong: "Wrong! Try again or skip to next.",
            errorLoading: "Error loading Pokemon.",
            errorChecking: "Error checking answer. Please try again.",
            aiUnavailable: "AI hint currently unavailable."
        },
        zh: {
            title: "猜猜这是哪只宝可梦？",
            score: "得分",
            attempts: "尝试次数",
            inputPlaceholder: "输入宝可梦名字...",
            submit: "提交答案",
            checking: "检查中...",
            skip: "跳过",
            aiAssistant: "AI 助手",
            getAiHint: "获取 AI 提示",
            gettingHint: "获取 AI 提示中...",
            aiPowered: "使用 Ollama 提供的 AI 提示",
            gameStats: "游戏统计",
            successRate: "成功率",
            hintsUsed: "已使用提示",
            aiHints: "AI 提示",
            loading: "加载宝可梦中...",
            correct: "正确！它是",
            wrong: "错误！再试一次或跳过。",
            errorLoading: "加载宝可梦出错。",
            errorChecking: "检查答案出错。请重试。",
            aiUnavailable: "AI 提示暂时不可用。"
        }
    };

    type TranslationKey = keyof typeof t.en;

    const getText = (key: TranslationKey): string => t[language][key];

    useEffect(() => {
        loadNewPokemon();
    }, []);

    const toggleLanguage= () =>{
        setLanguage(language=='en' ? 'zh':'en');
    }

    const loadNewPokemon = async () => {
        setLoading(true);
        setMessage('');
        setUserGuess('');
        setGameState('playing');
        setAiHint('');

        try {
            const response = await fetch(`${API_BASE}/pokemon/quiz`);
            const data = await response.json();
            setPokemon(data);
        } catch (error) {
            setMessage(getText('errorLoading'));
        } finally {
            setLoading(false);
        }
    };

    const getAiHint = async () => {
        if (!pokemon || loadingAiHint) return;

        setLoadingAiHint(true);
        try {
            const response = await fetch(`${API_BASE}/pokemon/ai-hint/${pokemon.id}?language=${language}`);
            const data = await response.json();
            setAiHint(data.hint);
        } catch (error) {
            setAiHint(getText('aiUnavailable'));
        } finally {
            setLoadingAiHint(false);
        }
    };

    const handleSubmit = async () => {
        if (!userGuess.trim() || !pokemon) return;

        setLoading(true);
        try {
            const response = await fetch(`${API_BASE}/pokemon/check`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    id: pokemon.id,
                    userAnswer: userGuess.trim()
                })
            });

            const data = await response.json();
            setAttempts(attempts + 1);

            if (data.correct) {
                setScore(score + 1);
                setMessage(`${getText('correct')} ${data.correctName}!`);
                setGameState('correct');
                setTimeout(() => loadNewPokemon(), 2500);
            } else {
                setMessage(getText('wrong'));
                setGameState('wrong');
            }
        }catch (error) {
            setMessage(getText('errorChecking'));

        } finally {
            setLoading(false);
        }
    };

    const handleKeyPress = (e: any) => {
        if (e.key === 'Enter' && !loading && userGuess.trim() && gameState !== 'correct') {
            handleSubmit();
        }
    };

    if (loading && !pokemon) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-white">
                <div className="text-gray-800 text-2xl font-bold animate-pulse">Loading Pokemon...</div>
            </div>
        );
    }

    return (
        <div className="min-h-screen pt-6 md:pt-10 bg-white">
            <div className="max-w-4xl mx-auto px-4">
                <div className="bg-white rounded-2xl shadow-2xl p-4 md:p-6 mb-6">
                    {/* Mobile: Two-line layout, Desktop: Single-line layout */}
                    <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                        {/* First line on mobile: Icon and Title */}
                        <div className="flex items-center gap-3">
                            <img src='/pokeball-png-45330.png' alt="Pokemon Logo" className="w-10 h-10 md:w-12 md:h-12"/>
                            <h1 className="text-2xl md:text-3xl font-bold text-gray-800">{getText('title')}</h1>
                        </div>

                        {/* Second line on mobile: Language button and stats */}
                        <div className="flex items-center justify-between md:justify-end gap-4 md:gap-6">
                            <button
                                onClick={toggleLanguage}
                                className="flex items-center gap-2 text-black border-black border-2 px-3 md:px-4 py-2 rounded-lg transform hover:scale-105 active:scale-95 shadow-md"
                            >
                                <span className="font-semibold text-sm md:text-base">{language === 'en' ? '中文' : 'EN'}</span>
                            </button>
                            <div className="text-center">
                                <div className="text-xl md:text-2xl font-bold text-gray-800">{score}</div>
                                <div className="text-xs text-gray-600">{getText('score')}</div>
                            </div>
                            <div className="text-center">
                                <div className="text-xl md:text-2xl font-bold text-gray-800">{attempts}</div>
                                <div className="text-xs text-gray-600">{getText('attempts')}</div>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    <div className="bg-white rounded-2xl shadow-2xl p-6 md:p-8">
                        <div className="aspect-square bg-gradient-to-br from-gray-50 to-gray-100 rounded-xl flex items-center justify-center mb-4 relative overflow-hidden">
                            {pokemon.imageUrl && (
                                <img
                                    src={pokemon.imageUrl}
                                    alt="Mystery Pokemon"
                                    className={`max-w-full max-h-full object-contain transition-all duration-500 ${
                                        gameState === 'correct' ? 'scale-110' : 'scale-100'
                                    }`}
                                    style={{
                                        filter: (gameState === 'playing' || gameState === 'wrong') ? 'brightness(0) saturate(100%)' : 'none'
                                    }}
                                />
                            )}
                        </div>

                        <div className="space-y-4">
                            <input
                                type="text"
                                value={userGuess}
                                onChange={(e) => setUserGuess(e.target.value)}
                                onKeyPress={handleKeyPress}
                                placeholder={getText('inputPlaceholder')}
                                disabled={gameState === 'correct' || loading}
                                className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:outline-none text-lg disabled:bg-gray-100 text-gray-800"
                            />

                            <div className="flex gap-3">
                                <button
                                    onClick={handleSubmit}
                                    disabled={gameState === 'correct' || loading || !userGuess.trim()}
                                    className="flex-1 bg-red-600 text-white py-3 rounded-lg font-bold disabled:opacity-50 disabled:cursor-not-allowed transition-all transform hover:scale-105 active:scale-95"
                                >
                                    {loading ? getText('checking') : getText('submit')}
                                </button>

                                <button
                                    onClick={loadNewPokemon}
                                    disabled={loading}
                                    className="px-6 bg-gray-200 text-gray-700 py-3 rounded-lg font-bold hover:bg-gray-300 disabled:opacity-50 transition-all"
                                >
                                    {getText('skip')}
                                </button>
                            </div>
                        </div>

                        {message && (
                            <div className={`mt-4 p-4 rounded-lg text-center font-bold ${
                                gameState === 'correct'
                                    ? 'bg-green-100 text-green-800'
                                    : 'bg-red-100 text-red-800'
                            }`}>
                                {message}
                            </div>
                        )}
                    </div>

                    <div className="space-y-6">

                        <div className="bg-white rounded-2xl shadow-2xl p-6">
                            <div className="flex items-center gap-2 mb-4">
                                <h2 className="text-xl font-bold text-gray-800">{getText('aiAssistant')}</h2>
                            </div>

                            {!aiHint ? (
                                <button
                                    onClick={getAiHint}
                                    disabled={loadingAiHint}
                                    className="w-full bg-gradient-to-r from-yellow-400 to-orange-400 text-white py-3 rounded-lg font-bold hover:from-yellow-500 hover:to-orange-500 disabled:opacity-50 transition-all transform hover:scale-105 active:scale-95"
                                >
                                    {loadingAiHint ? getText('gettingHint') : getText('getAiHint')}
                                </button>
                            ) : (
                                <div className="bg-gradient-to-r from-yellow-50 to-orange-50 p-4 rounded-lg border-2 border-yellow-200">
                                    <p className="text-gray-700 leading-relaxed">{aiHint}</p>
                                </div>
                            )}

                            <p className="text-xs text-gray-500 mt-3 text-center">
                                {getText('aiPowered')}
                            </p>
                        </div>

                        <div className="bg-white rounded-2xl shadow-2xl p-6">
                            <h2 className="text-xl font-bold text-gray-800 mb-4">{getText('gameStats')}</h2>
                            <div className="space-y-3">
                                <div className="flex justify-between items-center">
                                    <span className="text-gray-600">{getText('successRate')}</span>
                                    <span className="font-bold text-purple-600">
                                        {attempts > 0 ? Math.round((score / attempts) * 100) : 0}%
                                    </span>
                                </div>
                                <div className="flex justify-between items-center">
                                    <span className="text-gray-600">{getText('aiHints')}</span>
                                    <span className="font-bold text-yellow-600">{aiHint ? '1' : '0'}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PokemonGuessGame;
