import { useState, useCallback, useRef, useEffect } from 'react'

const ONES = ['zero', 'one', 'two', 'three', 'four', 'five', 'six', 'seven', 'eight', 'nine',
  'ten', 'eleven', 'twelve', 'thirteen', 'fourteen', 'fifteen', 'sixteen', 'seventeen', 'eighteen', 'nineteen']
const TENS = ['', '', 'twenty', 'thirty', 'forty', 'fifty', 'sixty', 'seventy', 'eighty', 'ninety']

function numberToWords(n) {
  if (n === 0) return 'zero'
  let abs = Math.abs(n)
  let words = ''
  if (abs >= 1000000000) { words += numberToWords(Math.floor(abs / 1000000000)) + ' billion '; abs %= 1000000000 }
  if (abs >= 1000000) { words += numberToWords(Math.floor(abs / 1000000)) + ' million '; abs %= 1000000 }
  if (abs >= 1000) { words += numberToWords(Math.floor(abs / 1000)) + ' thousand '; abs %= 1000 }
  if (abs >= 100) { words += numberToWords(Math.floor(abs / 100)) + ' hundred '; abs %= 100 }
  if (abs > 0) {
    if (words) words += 'and '
    if (abs < 20) words += ONES[abs]
    else { words += TENS[Math.floor(abs / 10)]; if (abs % 10) words += '-' + ONES[abs % 10] }
  }
  return words.trim()
}

function numberToWordsWithDecimal(numStr) {
  const parts = numStr.replace(/,/g, '').split('.')
  let result = numberToWords(parseInt(parts[0]))
  if (result === '') result = 'zero'
  if (parts.length > 1 && parts[1].length > 0) {
    const decimalDigits = parts[1].replace(/0+$/, '') || '0'
    if (decimalDigits !== '0') {
      result += ' point'
      for (const ch of decimalDigits) result += ' ' + ONES[parseInt(ch)]
    }
  }
  return result
}

const SQ_ONES = ['zero', 'një', 'dy', 'tre', 'katër', 'pesë', 'gjashtë', 'shtatë', 'tetë', 'nëntë',
  'dhjetë', 'njëmbëdhjetë', 'dymbëdhjetë', 'trembëdhjetë', 'katërmbëdhjetë', 'pesëmbëdhjetë',
  'gjashtëmbëdhjetë', 'shtatëmbëdhjetë', 'tetëmbëdhjetë', 'nëntëmbëdhjetë']
const SQ_TENS = ['', '', 'njëzet', 'tridhjetë', 'dyzet', 'pesëdhjetë', 'gjashtëdhjetë', 'shtatëdhjetë', 'tetëdhjetë', 'nëntëdhjetë']

function sqNumberToWords(n) {
  if (n === 0) return 'zero'
  let abs = Math.abs(n)
  let words = ''
  if (abs >= 1000000000) { words += sqNumberToWords(Math.floor(abs / 1000000000)) + ' miliard '; abs %= 1000000000 }
  if (abs >= 1000000) { words += sqNumberToWords(Math.floor(abs / 1000000)) + ' milion '; abs %= 1000000 }
  if (abs >= 1000) { words += sqNumberToWords(Math.floor(abs / 1000)) + ' mijë '; abs %= 1000 }
  if (abs >= 100) { words += sqNumberToWords(Math.floor(abs / 100)) + 'qind '; abs %= 100 }
  if (abs > 0) {
    if (words) words += 'e '
    if (abs < 20) words += SQ_ONES[abs]
    else { words += SQ_TENS[Math.floor(abs / 10)]; if (abs % 10) words += ' e ' + SQ_ONES[abs % 10] }
  }
  return words.trim()
}

const UNIT_MAP = {
  'gb': 'gigabytes', 'mb': 'megabytes', 'kb': 'kilobytes',
  'gbps': 'gigabits per second', 'mbps': 'megabits per second',
  'km': 'kilometers', 'kg': 'kilograms', 'cm': 'centimeters',
  'mm': 'millimeters', 'hr': 'hours', 'min': 'minutes',
  'eur': 'euros', 'lek': 'Lek',
}

const UNIT_MAP_SQ = {
  'gb': 'gigabajt', 'mb': 'megabajt', 'kb': 'kilobajt',
  'gbps': 'gigabit për sekondë', 'mbps': 'megabit për sekondë',
  'km': 'kilometra', 'kg': 'kilogramë', 'cm': 'centimetra',
  'mm': 'milimetra', 'hr': 'orë', 'min': 'minuta',
}

const ABBREVIATIONS = {
  'vs': 'versus', 'etc': 'etcetera', 'e.g.': 'for example',
  'i.e.': 'that is', 'w/': 'with', 'w/o': 'without',
}

function prepareForTTS(text, lang) {
  if (lang === 'sq') {
    let t = text
      .replace(/\*\*/g, '')
      .replace(/\n{2,}/g, '. ')
      .replace(/\n/g, '. ')

    t = t.replace(/(\d+)\s*(GB|MB|KB|gb|mb|kb)\b/g, (_, num, unit) => {
      const n = parseInt(num)
      return sqNumberToWords(n) + ' ' + (n === 1 ? unit.toLowerCase() : (UNIT_MAP_SQ[unit.toLowerCase()] || unit))
    })
    t = t.replace(/(\d+)\s*(gbps|mbps|Gbps|Mbps)\b/g, (_, num, unit) =>
      sqNumberToWords(parseInt(num)) + ' ' + (UNIT_MAP_SQ[unit.toLowerCase()] || unit))
    t = t.replace(/(\d+(?:,\d{3})*(?:\.\d+)?)\s*Lek/gi, (_, num) => {
      const clean = num.replace(/,/g, '')
      return sqNumberToWords(Math.round(parseFloat(clean))) + ' lekë'
    })
    t = t.replace(/(\d+(?:,\d{3})*(?:\.\d+)?)/g, (_, num) => {
      const clean = num.replace(/,/g, '')
      return sqNumberToWords(Math.round(parseFloat(clean)))
    })

    t = t.replace(/\s+/g, ' ').trim()
    return t
  }

  let t = text
    .replace(/\*\*/g, '')
    .replace(/\n{2,}/g, '. ')
    .replace(/\n/g, '. ')

  const abbrPattern = new RegExp('\\b(' + Object.keys(ABBREVIATIONS).join('|') + ')\\b', 'gi')
  t = t.replace(abbrPattern, m => ABBREVIATIONS[m.toLowerCase()] || m)

  t = t.replace(/(\d+)\s*%\s*/g, (_, num) => numberToWords(parseInt(num)) + ' percent ')
  t = t.replace(/(\d+)\s*(GB|MB|KB|gb|mb|kb)\b/g, (_, num, unit) => {
    const n = parseInt(num)
    return numberToWords(n) + ' ' + (n === 1 ? unit.toLowerCase() : (UNIT_MAP[unit.toLowerCase()] || unit))
  })
  t = t.replace(/(\d+)\s*(gbps|mbps|Gbps|Mbps)\b/g, (_, num, unit) =>
    numberToWords(parseInt(num)) + ' ' + (UNIT_MAP[unit.toLowerCase()] || unit))
  t = t.replace(/(\d+(?:,\d{3})*(?:\.\d+)?)\s*(EUR|eur)/g, (_, num) =>
    numberToWordsWithDecimal(num) + ' euros')
  t = t.replace(/(\d+(?:,\d{3})*(?:\.\d+)?)\s*Lek/gi, (_, num) =>
    numberToWordsWithDecimal(num) + ' Lek')
  t = t.replace(/(\d+)\s*\/\s*(\d+)/g, (_, a, b) => numberToWords(parseInt(a)) + ' out of ' + numberToWords(parseInt(b)))
  t = t.replace(/\b(\d+)G\b/g, (_, n) => numberToWords(parseInt(n)) + ' G')
  t = t.replace(/(\d+(?:,\d{3})*(?:\.\d+)?)/g, (_, num) => numberToWordsWithDecimal(num))

  t = t.replace(/\s+/g, ' ').trim()
  return t
}

function detectLanguage(text) {
  const sqWords = ['pershendetje', 'tungjatjeta', 'përshëndetje', 'kredi', 'fatur', 'mbush',
    'faleminderit', 'ndihm', 'miredita', 'mirëdita', 'lek', 'jam', 'shqip',
    'paguaj', 'borxh', 'kariko', 'oferta', 'planin', 'abonim', 'rrjet', 'miremengjes',
    'mirmengjes', 'natemire', 'natën', 'tung', 'çfarë', 'cfare', 'cilat', 'sa']
  const m = text.toLowerCase()
  for (const w of sqWords) { if (m.includes(w)) return 'sq' }
  if (m.includes('ë') || m.includes('ç')) return 'sq'
  return 'en'
}

const VOICE_CACHE = {}

async function findBestVoice(lang) {
  if (VOICE_CACHE[lang]) return VOICE_CACHE[lang]
  return new Promise((resolve) => {
    const tryResolve = () => {
      let voices = window.speechSynthesis.getVoices()
      if (voices.length === 0) { setTimeout(tryResolve, 100); return }
      const langPrefix = lang === 'sq' ? 'sq' : 'en'
      const premium = voices.filter(v =>
        v.lang.startsWith(langPrefix) && !v.name.toLowerCase().includes('microsoft') &&
        (v.name.toLowerCase().includes('premium') || v.name.toLowerCase().includes('natural') ||
         v.name.toLowerCase().includes('neural') || v.name.toLowerCase().includes('google') ||
         v.name.toLowerCase().includes('enhanced') || v.name.toLowerCase().includes('wavenet') ||
         v.name.toLowerCase().includes('studio') || v.name.toLowerCase().includes('standard')))
      const exact = voices.filter(v => v.lang.startsWith(langPrefix))
      const selected = premium.length > 0 ? premium[premium.length - 1]
        : exact.length > 0 ? exact[exact.length - 1] : null
      VOICE_CACHE[lang] = selected
      resolve(selected)
    }
    tryResolve()
  })
}

export function useSpeech() {
  const [isListening, setIsListening] = useState(false)
  const [isSpeaking, setIsSpeaking] = useState(false)
  const [transcript, setTranscript] = useState('')
  const [interimText, setInterimText] = useState('')
  const [error, setError] = useState(null)
  const [browserSupported, setBrowserSupported] = useState(false)
  const [isUsingFallback, setIsUsingFallback] = useState(false)
  const recognitionRef = useRef(null)
  const mediaRecorderRef = useRef(null)
  const chunksRef = useRef([])
  const streamRef = useRef(null)

  useEffect(() => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
    setBrowserSupported(!!SpeechRecognition)
  }, [])

  const clearError = useCallback(() => setError(null), [])

  const startListening = useCallback(async () => {
    setError(null)
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition

    if (SpeechRecognition) {
      try {
        const recognition = new SpeechRecognition()
        recognition.continuous = false
        recognition.interimResults = true
        recognition.maxAlternatives = 3
        recognition.lang = 'sq-AL'
        recognition.onresult = (event) => {
          let interim = '', final = ''
          for (let i = event.resultIndex; i < event.results.length; i++) {
            if (event.results[i].isFinal) final += event.results[i][0].transcript
            else interim += event.results[i][0].transcript
          }
          if (final) setTranscript(final)
          setInterimText(interim || final)
        }
        recognition.onend = () => { setIsListening(false); setInterimText('') }
        recognition.onerror = (event) => {
          setIsListening(false)
          setInterimText('')
          switch (event.error) {
            case 'not-allowed':
              setError('Lejo mikrofonin në shfletues për të përdorur zërin.')
              break
            case 'no-speech':
              setError('Nuk u dëgjua asnjë fjalë. Provo përsëri.')
              break
            case 'audio-capture':
              setError('Nuk u gjet mikrofon. Kontrollo pajisjen tënde.')
              break
            case 'service-not-allowed':
              setError('Shërbimi i zërit nuk lejohet në këtë faqe.')
              break
            case 'aborted':
              break
            default:
              setError('Gabim në mikrofon: ' + event.error)
          }
        }
        recognitionRef.current = recognition
        recognition.start()
        setIsListening(true)
        setInterimText('Dëgjohet...')
      } catch (e) {
        setError('Nuk mund të nisej mikrofoni. Provo përsëri.')
      }
    } else {
      setIsUsingFallback(true)
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
        streamRef.current = stream
        const mimeType = MediaRecorder.isTypeSupported('audio/webm;codecs=opus')
          ? 'audio/webm;codecs=opus'
          : 'audio/webm'
        const mediaRecorder = new MediaRecorder(stream, { mimeType })
        mediaRecorderRef.current = mediaRecorder
        chunksRef.current = []

        mediaRecorder.ondataavailable = (e) => {
          if (e.data.size > 0) chunksRef.current.push(e.data)
        }

        mediaRecorder.onstop = async () => {
          setIsListening(false)
          setInterimText('Duke transkriptuar...')
          const audioBlob = new Blob(chunksRef.current, { type: 'audio/webm' })
          stream.getTracks().forEach(t => t.stop())

          try {
            const formData = new FormData()
            formData.append('audio', audioBlob, 'recording.webm')
            const response = await fetch('/api/tobi2/stt', { method: 'POST', body: formData })
            if (!response.ok) throw new Error('STT request failed')
            const data = await response.json()
            if (data.text) {
              setTranscript(data.text)
            } else {
              setError('Transkriptimi dështoi. Provo përsëri.')
            }
          } catch (err) {
            setError('Transkriptimi në server dështoi. Provo përsëri.')
          }
          setIsUsingFallback(false)
        }

        mediaRecorder.onerror = () => {
          setIsListening(false)
          setIsUsingFallback(false)
          setError('Gabim gjatë regjistrimit të zërit.')
        }

        mediaRecorder.start()
        setIsListening(true)
        setInterimText('Duke regjistruar...')
      } catch (err) {
        setIsUsingFallback(false)
        if (err.name === 'NotAllowedError') {
          setError('Lejo mikrofonin në shfletues për të përdorur zërin.')
        } else if (err.name === 'NotFoundError') {
          setError('Nuk u gjet mikrofon. Kontrollo pajisjen tënde.')
        } else {
          setError('Nuk mund të qasej mikrofoni. (' + err.message + ')')
        }
      }
    }
  }, [])

  const stopListening = useCallback(() => {
    if (recognitionRef.current) {
      try { recognitionRef.current.stop() } catch (e) { /* ignore */ }
      setIsListening(false)
      setInterimText('')
      recognitionRef.current = null
    }
    if (mediaRecorderRef.current && mediaRecorderRef.current.state === 'recording') {
      mediaRecorderRef.current.stop()
      mediaRecorderRef.current = null
    }
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(t => t.stop())
      streamRef.current = null
    }
    setIsListening(false)
    setInterimText('')
  }, [])

  const speak = useCallback(async (text, lang) => {
    try {
      const synth = window.speechSynthesis
      if (!synth) throw new Error('Speech synthesis not available')
      synth.cancel()
      const prepared = prepareForTTS(text, lang || detectLanguage(text))
      const utterance = new SpeechSynthesisUtterance(prepared)
      utterance.lang = 'sq-AL'
      utterance.rate = 0.9
      utterance.pitch = 1.05
      utterance.volume = 1.0
      return new Promise((resolve) => {
        utterance.onstart = () => setIsSpeaking(true)
        utterance.onend = () => { setIsSpeaking(false); resolve() }
        utterance.onerror = (e) => { console.warn('Speech error:', e); setIsSpeaking(false); resolve() }
        synth.speak(utterance)
      })
    } catch (e) {
      console.warn('Speech failed:', e)
    }
  }, [])

  const stopSpeaking = useCallback(() => {
    window.speechSynthesis?.cancel()
    setIsSpeaking(false)
  }, [])

  return { isListening, isSpeaking, transcript, interimText, error, browserSupported, isUsingFallback, clearError, startListening, stopListening, speak, stopSpeaking }
}
