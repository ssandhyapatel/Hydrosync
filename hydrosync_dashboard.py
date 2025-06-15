
import streamlit as st
import pandas as pd
import numpy as np
import plotly.express as px

# Simulate or load dataset
np.random.seed(42)
dates = pd.date_range(start="2024-01-01", periods=90)
df = pd.DataFrame({
    'Date': np.random.choice(dates, size=1500),
    'HydrationLevel(%)': np.random.uniform(40, 100, 1500),
    'FatigueScore(0-100)': np.random.uniform(20, 90, 1500),
    'SleepHours': np.random.uniform(4, 9, 1500),
    'HeartRate': np.random.randint(60, 110, 1500),
    'ActivityLevel': np.random.choice(['Low', 'Moderate', 'High'], size=1500),
    'Age': np.random.randint(16, 60, 1500),
    'Gender': np.random.choice(['Male', 'Female', 'Other'], size=1500)
})
df['Date'] = pd.to_datetime(df['Date'])

# Sidebar filters
st.sidebar.header("Filter Data")
gender_filter = st.sidebar.multiselect("Select Gender", options=df['Gender'].unique(), default=df['Gender'].unique())
activity_filter = st.sidebar.multiselect("Select Activity Level", options=df['ActivityLevel'].unique(), default=df['ActivityLevel'].unique())
date_range = st.sidebar.date_input("Select Date Range", [df['Date'].min(), df['Date'].max()])

# Filter data
filtered_df = df[
    (df['Gender'].isin(gender_filter)) &
    (df['ActivityLevel'].isin(activity_filter)) &
    (df['Date'] >= pd.to_datetime(date_range[0])) &
    (df['Date'] <= pd.to_datetime(date_range[1]))
]

st.title("💧 HydroSync Interactive Dashboard")
st.markdown("Track hydration, fatigue, and wellness insights interactively.")

# Charts
tab1, tab2, tab3 = st.tabs(["📈 Trends", "📊 Summary Stats", "🧠 Correlation"])

with tab1:
    st.subheader("Hydration Over Time")
    fig1 = px.line(filtered_df.groupby("Date")["HydrationLevel(%)"].mean().reset_index(), x="Date", y="HydrationLevel(%)")
    st.plotly_chart(fig1)

    st.subheader("Fatigue Over Time")
    fig2 = px.line(filtered_df.groupby("Date")["FatigueScore(0-100)"].mean().reset_index(), x="Date", y="FatigueScore(0-100)")
    st.plotly_chart(fig2)

with tab2:
    st.subheader("Aggregate Summary")
    st.dataframe(filtered_df.describe())

with tab3:
    st.subheader("Correlation Heatmap")
    numeric_cols = filtered_df.select_dtypes(include=np.number)
    corr = numeric_cols.corr()
    fig_corr = px.imshow(corr, text_auto=True, aspect="auto", color_continuous_scale="RdBu_r", title="Correlation Matrix")
    st.plotly_chart(fig_corr)
